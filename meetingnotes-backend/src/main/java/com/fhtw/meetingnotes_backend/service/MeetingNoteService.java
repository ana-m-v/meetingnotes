package com.fhtw.meetingnotes_backend.service;

import com.fhtw.meetingnotes_backend.domain.ActionItem;
import com.fhtw.meetingnotes_backend.domain.MeetingNote;
import com.fhtw.meetingnotes_backend.domain.OutboxEvent;
import com.fhtw.meetingnotes_backend.dto.request.MeetingNoteRequest;
import com.fhtw.meetingnotes_backend.dto.request.UpdateActionItemRequest;
import com.fhtw.meetingnotes_backend.dto.response.ActionItemResponse;
import com.fhtw.meetingnotes_backend.dto.response.IngestResponse;
import com.fhtw.meetingnotes_backend.dto.response.MeetingNoteResponse;
import com.fhtw.meetingnotes_backend.repository.ActionItemRepository;
import com.fhtw.meetingnotes_backend.repository.MeetingNoteRepository;
import com.fhtw.meetingnotes_backend.repository.OutboxEventRepository;
import com.fhtw.meetingnotes_backend.service.events.NoteIngestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class MeetingNoteService {

    private static final Logger log = LoggerFactory.getLogger(MeetingNoteService.class);

    private final MeetingNoteRepository noteRepo;
    private final ActionItemRepository actionRepo;
    private final OutboxEventRepository outboxRepo;
    private final ApplicationEventPublisher eventPublisher;

    public MeetingNoteService(MeetingNoteRepository noteRepo,
                              ActionItemRepository actionRepo,
                              OutboxEventRepository outboxRepo,
                              ApplicationEventPublisher eventPublisher) {
        this.noteRepo = noteRepo;
        this.actionRepo = actionRepo;
        this.outboxRepo = outboxRepo;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public IngestResponse ingest(MeetingNoteRequest req) {
        String hash = sha256(req.getContent());

        var existing = noteRepo.findByContentHash(hash);
        if (existing.isPresent()) {
            log.info("Duplicate ingestion detected for hash={}", hash);
            IngestResponse resp = new IngestResponse();
            resp.setNote(MeetingNoteResponse.from(existing.get()));
            resp.setDuplicate(true);
            return resp;
        }

        MeetingNote note = new MeetingNote();
        note.setTitle(req.getTitle());
        note.setRawContent(req.getContent());
        note.setContentHash(hash);
        note.setParseStatus(MeetingNote.ParseStatus.IN_PROGRESS);

        try {
            noteRepo.save(note);
        } catch (DataIntegrityViolationException e) {
            log.info("Concurrent duplicate insert detected for hash={}, re-querying", hash);
            MeetingNote winner = noteRepo.findByContentHash(hash).orElseThrow();
            IngestResponse resp = new IngestResponse();
            resp.setNote(MeetingNoteResponse.from(winner));
            resp.setDuplicate(true);
            return resp;
        }

        final long noteId = note.getId();
        final String rawContent = req.getContent();
        // Publish after commit to avoid async parse reading an uncommitted note.
        eventPublisher.publishEvent(new NoteIngestedEvent(noteId, rawContent));

        IngestResponse resp = new IngestResponse();
        resp.setNote(MeetingNoteResponse.from(note));
        resp.setDuplicate(false);
        return resp;
    }

    @Transactional
    public void persistParsedItems(long noteId, List<ActionItem> items) {
        MeetingNote note = noteRepo.findById(noteId).orElseThrow();
        items.forEach(a -> a.setMeetingNote(note));
        actionRepo.saveAll(items);
        note.setParseStatus(MeetingNote.ParseStatus.DONE);
        note.setParsedAt(Instant.now());
        noteRepo.save(note);
        log.info("Parsed noteId={} — {} action items found", noteId, items.size());
    }

    @Transactional
    public void markFailed(long noteId) {
        noteRepo.findById(noteId).ifPresent(n -> {
            n.setParseStatus(MeetingNote.ParseStatus.FAILED);
            noteRepo.save(n);
        });
    }

    @Transactional(readOnly = true)
    public List<ActionItemResponse> getActionItems(Long noteId) {
        return actionRepo.findByMeetingNoteId(noteId).stream()
                .map(ActionItemResponse::from)
                .toList();
    }

    @Transactional
    public ActionItemResponse updateActionItem(Long itemId, UpdateActionItemRequest req) {
        int attempts = 0;
        while (true) {
            try {
                ActionItem item = actionRepo.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("Action item not found: " + itemId));

                if (req.getVersion() != null && !req.getVersion().equals(item.getVersion())) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Action item was updated by someone else. Reload and retry."
                    );
                }

                if (req.getOwner() != null)   item.setOwner(req.getOwner());
                if (req.getDueDate() != null) item.setDueDate(req.getDueDate());
                if (req.getStatus() != null)  item.setStatus(req.getStatus());
                item.setUpdatedAt(Instant.now());
                actionRepo.save(item);

                if (req.isScheduleReminder() && req.getOwnerEmail() != null) {
                    Instant when = req.getReminderAt() != null ? req.getReminderAt() : Instant.now().plusSeconds(60);
                    OutboxEvent evt = OutboxEvent.reminder(itemId, req.getOwnerEmail(), item.getDescription(), when);
                    outboxRepo.save(evt);
                    log.info("Scheduled reminder for actionItemId={} at {}", itemId, when);
                }

                return ActionItemResponse.from(item);

            } catch (ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= 3) throw e;
                log.warn("Optimistic lock conflict on actionItemId={}, retrying (attempt {})", itemId, attempts);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<MeetingNoteResponse> listNotes() {
        return noteRepo.findAll().stream()
                .map(MeetingNoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MeetingNoteResponse getNote(Long id) {
        return noteRepo.findById(id)
                .map(MeetingNoteResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Note not found: " + id));
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
