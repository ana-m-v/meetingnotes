package com.fhtw.meetingnotes_backend.outbox;

import com.fhtw.meetingnotes_backend.domain.OutboxEvent;
import com.fhtw.meetingnotes_backend.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxDispatcher {

    private final OutboxEventRepository outboxRepo;

    /**
     * Each dispatch runs in its own NEW transaction.
     *
     * Step 1: atomically claim the event with UPDATE WHERE status = PENDING.
     *         If another thread already claimed it, claimEvent() returns 0 - we skip.
     * Step 2: do the actual delivery (email, etc.)
     * Step 3: mark SENT or back to PENDING for retry (or FAILED after max retries)
     *
     * REQUIRES_NEW ensures each event's transaction commits independently -
     * one failed delivery doesn't roll back the others.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(Long eventId) {
        // Atomic claim - only one thread wins this UPDATE
        int claimed = outboxRepo.claimEvent(eventId);
        if (claimed == 0) {
            log.debug("Event id={} already claimed by another thread, skipping", eventId);
            return;
        }

        OutboxEvent event = outboxRepo.findById(eventId).orElseThrow();

        try {
            if ("REMINDER".equals(event.getEventType())) {
                sendReminderEmail(event);
            }
            event.setStatus(OutboxEvent.EventStatus.SENT);
            event.setProcessedAt(Instant.now());
            log.info("Outbox event id={} dispatched successfully", eventId);
        } catch (Exception ex) {
            log.error("Failed to dispatch outbox event id={}: {}", eventId, ex.getMessage());
            event.setRetryCount(event.getRetryCount() + 1);
            event.setLastError(ex.getMessage());
            event.setStatus(event.getRetryCount() >= 3
                    ? OutboxEvent.EventStatus.FAILED   // dead letter - stop retrying
                    : OutboxEvent.EventStatus.PENDING  // back to queue for next tick
            );
        }
        outboxRepo.save(event);
    }

    private void sendReminderEmail(OutboxEvent event) {
        String payload = event.getPayload();
        String email = extractJsonString(payload, "ownerEmail");
        String desc = extractJsonString(payload, "description");

        if (email == null || email.isBlank()) {
            log.warn("Reminder event id={} has no ownerEmail - skipping", event.getId());
            return;
        }

        log.info(
                "REMINDER EMAIL (mock) to={} subject='Reminder: Action item due' body='{}' eventId={}",
                email,
                "Don't forget your action item:\n\n" + desc,
                event.getId()
        );
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? null : json.substring(start, end);
    }
}
