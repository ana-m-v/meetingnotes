package com.fhtw.meetingnotes_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Transactional Outbox Pattern:
 * Instead of calling mail/notification directly (fire-and-forget, no retry),
 * we write an OutboxEvent in the SAME transaction as the business operation.
 * A separate scheduler polls and dispatches pending events, giving us
 * at-least-once delivery with retry on failure.
 */
@Entity
@Table(name = "outbox_events",
        indexes = @Index(columnList = "status, scheduled_at"))
@Getter @Setter @NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;   // e.g. "REMINDER", "PARSE_COMPLETE"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;     // JSON payload

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.PENDING;

    @Column(nullable = false)
    private Instant scheduledAt = Instant.now();  // when to send

    @Column
    private Instant processedAt;

    @Column(nullable = false)
    private int retryCount = 0;

    @Column
    private String lastError;

    public enum EventStatus {
        PENDING, PROCESSING, SENT, FAILED
    }

    public static OutboxEvent reminder(Long actionItemId, String ownerEmail, String description, Instant when) {
        OutboxEvent e = new OutboxEvent();
        e.eventType = "REMINDER";
        e.payload = String.format(
                "{\"actionItemId\":%d,\"ownerEmail\":\"%s\",\"description\":\"%s\"}",
                actionItemId, ownerEmail, description.replace("\"", "'")
        );
        e.scheduledAt = when;
        return e;
    }
}
