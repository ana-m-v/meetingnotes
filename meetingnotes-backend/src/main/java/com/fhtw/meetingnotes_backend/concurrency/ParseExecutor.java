package com.fhtw.meetingnotes_backend.concurrency;

import com.fhtw.meetingnotes_backend.domain.ActionItem;
import com.fhtw.meetingnotes_backend.domain.MeetingNote;
import com.fhtw.meetingnotes_backend.service.MeetingNotesParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

/**
 * Concurrency patterns used here:
 *
 * 1. Worker Thread    — FixedThreadPool isolates parse work from HTTP threads
 * 2. Backpressure     — Semaphore blocks new submissions when too many are in-flight
 * 3. Async / Future   — CompletableFuture lets the HTTP response return immediately
 * 4. Scatter-Gather   — submitBatchAsync fans out all notes concurrently then joins
 *
 * Thread safety fix: we no longer accept a MeetingNote entity across the thread
 * boundary. Instead we accept (noteId, rawContent) — plain values that are safe
 * to share. The entity is built locally inside the task so no JPA session is shared.
 */
@Component
@Slf4j
public class ParseExecutor {

    private final ExecutorService pool;
    private final Semaphore semaphore;
    private final MeetingNotesParser parser;

    public ParseExecutor(
            MeetingNotesParser parser,
            @Value("${app.concurrency.parse-threads:4}") int threads,
            @Value("${app.concurrency.parse-semaphore-permits:10}") int permits) {
        this.parser = parser;
        this.pool = Executors.newFixedThreadPool(threads);
        this.semaphore = new Semaphore(permits);
    }

    /**
     * Submit a single note for async parsing.
     * Accepts plain values only — no JPA entities cross the thread boundary.
     *
     * Backpressure happens BEFORE a worker thread is used: the caller waits
     * for a permit, then the task is submitted to the pool.
     */
    public CompletableFuture<List<ActionItem>> submitAsync(long noteId, String rawContent) {
        try {
            semaphore.acquire(); // backpressure: block if too many concurrent parses
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            CompletableFuture<List<ActionItem>> failed = new CompletableFuture<>();
            failed.completeExceptionally(new RuntimeException("Interrupted while waiting for parse slot", e));
            return failed;
        }

        log.info("Parsing noteId={} (available permits: {})",
                noteId, semaphore.availablePermits());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build a detached note shell just for parsing — no JPA session involved
                MeetingNote shell = new MeetingNote();
                shell.setRawContent(rawContent);
                return parser.parse(shell);
            } finally {
                semaphore.release();
            }
        }, pool);
    }


}
