package com.fhtw.meetingnotes_backend.service;

import com.fhtw.meetingnotes_backend.domain.OutboxEvent;
import com.fhtw.meetingnotes_backend.dto.request.MeetingNoteRequest;
import com.fhtw.meetingnotes_backend.dto.request.ParseBatchBenchmarkRequest;
import com.fhtw.meetingnotes_backend.dto.response.IngestResponse;
import com.fhtw.meetingnotes_backend.dto.response.ParseBatchBenchmarkResponse;
import com.fhtw.meetingnotes_backend.dto.response.ReminderBenchmarkResponse;
import com.fhtw.meetingnotes_backend.repository.ActionItemRepository;
import com.fhtw.meetingnotes_backend.repository.MeetingNoteRepository;
import com.fhtw.meetingnotes_backend.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BenchmarkService {

    private final MeetingNoteService meetingNoteService;
    private final MeetingNoteRepository noteRepo;
    private final ActionItemRepository actionRepo;
    private final OutboxEventRepository outboxRepo;

    public ParseBatchBenchmarkResponse runParseBatchBenchmark(ParseBatchBenchmarkRequest req) {
        int requestedNotes = req.getNoteCount();
        int createdNotes = 0;
        int duplicateNotes = 0;

        long actionsBefore = actionRepo.count();
        Instant startedAt = Instant.now();

        for (int i = 0; i < requestedNotes; i++) {
            MeetingNoteRequest noteReq = new MeetingNoteRequest();
            noteReq.setTitle("Benchmark Note " + i);
            noteReq.setContent(buildSyntheticNote(i, req.getActionsPerNote(), req.getNoiseLinesPerNote(), req.isUniqueNotes()));

            IngestResponse resp = meetingNoteService.ingest(noteReq);
            if (resp.isDuplicate()) {
                duplicateNotes++;
            } else {
                createdNotes++;
            }
        }

        boolean completed = waitForParsesToFinish(req.getTimeoutSeconds());

        long actionsAfter = actionRepo.count();
        long extractedThisRun = actionsAfter - actionsBefore;
        long elapsedMillis = Duration.between(startedAt, Instant.now()).toMillis();
        double elapsedSeconds = Math.max(0.001, elapsedMillis / 1000.0);

        ParseBatchBenchmarkResponse resp = new ParseBatchBenchmarkResponse();
        resp.setRequestedNotes(requestedNotes);
        resp.setCreatedNotes(createdNotes);
        resp.setDuplicateNotes(duplicateNotes);
        resp.setTotalExtractedActions((int) extractedThisRun);
        resp.setElapsedMillis(elapsedMillis);
        resp.setActionsPerSecond(extractedThisRun / elapsedSeconds);
        resp.setNotesPerSecond(createdNotes / elapsedSeconds);
        resp.setCompletedWithinTimeout(completed);
        return resp;
    }

    public ReminderBenchmarkResponse computeReminderLatencyBenchmark() {
        List<OutboxEvent> sent = outboxRepo.findByEventTypeAndStatusAndProcessedAtIsNotNullOrderByProcessedAtAsc(
                "REMINDER",
                OutboxEvent.EventStatus.SENT
        );

        List<Long> latencies = sent.stream()
                .filter(e -> e.getScheduledAt() != null && e.getProcessedAt() != null)
                .map(e -> Duration.between(e.getScheduledAt(), e.getProcessedAt()).toMillis())
                .sorted()
                .toList();

        ReminderBenchmarkResponse resp = new ReminderBenchmarkResponse();
        resp.setSampleSize(latencies.size());

        if (latencies.isEmpty()) {
            resp.setMinMillis(0);
            resp.setP50Millis(0);
            resp.setP95Millis(0);
            resp.setMaxMillis(0);
            resp.setAvgMillis(0);
            return resp;
        }

        resp.setMinMillis(latencies.get(0));
        resp.setP50Millis(percentile(latencies, 0.50));
        resp.setP95Millis(percentile(latencies, 0.95));
        resp.setMaxMillis(latencies.get(latencies.size() - 1));
        resp.setAvgMillis(latencies.stream().mapToLong(Long::longValue).average().orElse(0));
        return resp;
    }

    private boolean waitForParsesToFinish(long timeoutSeconds) {
        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);

        while (Instant.now().isBefore(deadline)) {
            long inProgress = noteRepo.countByParseStatus(com.fhtw.meetingnotes_backend.domain.MeetingNote.ParseStatus.IN_PROGRESS);
            long pending = noteRepo.countByParseStatus(com.fhtw.meetingnotes_backend.domain.MeetingNote.ParseStatus.PENDING);

            if (inProgress == 0 && pending == 0) {
                return true;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    private long percentile(List<Long> sortedValues, double p) {
        if (sortedValues.isEmpty()) return 0;
        int index = (int) Math.ceil(p * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return sortedValues.get(index);
    }

    private String buildSyntheticNote(int noteIndex, int actionsPerNote, int noiseLinesPerNote, boolean uniqueNotes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sprint Planning Benchmark ").append(uniqueNotes ? noteIndex : 1).append("\n");
        sb.append("Attendees: Anna, Tom, Sarah, Max\n");
        sb.append("Discussion: roadmap, blockers, customer feedback\n\n");

        for (int i = 0; i < noiseLinesPerNote; i++) {
            sb.append("General discussion line ").append(i).append(" about priorities and project context.\n");
        }

        for (int i = 0; i < actionsPerNote; i++) {
            sb.append("- [ ] Action ")
                    .append(i)
                    .append(" for note ")
                    .append(uniqueNotes ? noteIndex : 1)
                    .append(" owner: anna due: 2026-03-31\n");
        }

        return sb.toString();
    }

    public String seedReminderEvents(int count, String ownerEmail) {
        for (int i = 0; i < count; i++) {
            OutboxEvent evt = OutboxEvent.reminder(
                    -1L,
                    ownerEmail,
                    "Benchmark reminder " + i,
                    Instant.now()
            );
            outboxRepo.save(evt);
        }
        return "Seeded " + count + " reminder events";
    }
}