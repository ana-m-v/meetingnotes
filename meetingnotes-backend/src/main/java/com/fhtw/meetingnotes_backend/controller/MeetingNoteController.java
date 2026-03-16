package com.fhtw.meetingnotes_backend.controller;
import com.fhtw.meetingnotes_backend.dto.request.MeetingNoteRequest;
import com.fhtw.meetingnotes_backend.dto.request.UpdateActionItemRequest;
import com.fhtw.meetingnotes_backend.dto.response.ActionItemResponse;
import com.fhtw.meetingnotes_backend.dto.response.IngestResponse;
import com.fhtw.meetingnotes_backend.dto.response.MeetingNoteResponse;
import com.fhtw.meetingnotes_backend.dto.response.OutboxEventResponse;
import com.fhtw.meetingnotes_backend.repository.OutboxEventRepository;
import com.fhtw.meetingnotes_backend.service.MeetingNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MeetingNoteController {

    private final MeetingNoteService service;
    private final OutboxEventRepository outboxRepo;

    @PostMapping("/notes")
    public ResponseEntity<IngestResponse> ingest(@Valid @RequestBody MeetingNoteRequest req) {
        IngestResponse resp = service.ingest(req);
        return resp.isDuplicate()
                ? ResponseEntity.ok(resp)
                : ResponseEntity.status(201).body(resp);
    }

    @GetMapping("/notes")
    public List<MeetingNoteResponse> listNotes() {
        return service.listNotes();
    }

    @GetMapping("/notes/{id}")
    public MeetingNoteResponse getNote(@PathVariable Long id) {
        return service.getNote(id);
    }

    @GetMapping("/notes/{id}/actions")
    public List<ActionItemResponse> getActionItems(@PathVariable Long id) {
        return service.getActionItems(id);
    }

    /**
     * 200 — updated successfully
     * 409 — optimistic lock conflict: someone else updated this item since you loaded it.
     *        Frontend should reload and let the user reapply their changes.
     */
    @PatchMapping("/actions/{id}")
    public ActionItemResponse updateActionItem(
            @PathVariable Long id,
            @RequestBody UpdateActionItemRequest req) {
        return service.updateActionItem(id, req);
    }

    @GetMapping("/reminders")
    public List<OutboxEventResponse> getReminders() {
        return outboxRepo.findTop50ByOrderByScheduledAtDesc().stream()
                .map(OutboxEventResponse::from)
                .toList();
    }
}