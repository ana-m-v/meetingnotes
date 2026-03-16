package com.fhtw.meetingnotes_backend.dto.response;

import com.fhtw.meetingnotes_backend.domain.OutboxEvent;
import java.time.Instant;

public class OutboxEventResponse {

    private Long id;
    private String eventType;
    private String payload;
    private OutboxEvent.EventStatus status;
    private Instant scheduledAt;
    private Instant processedAt;
    private int retryCount;
    private String lastError;

    public static OutboxEventResponse from(OutboxEvent e) {
        OutboxEventResponse r = new OutboxEventResponse();
        r.id = e.getId();
        r.eventType = e.getEventType();
        r.payload = e.getPayload();
        r.status = e.getStatus();
        r.scheduledAt = e.getScheduledAt();
        r.processedAt = e.getProcessedAt();
        r.retryCount = e.getRetryCount();
        r.lastError = e.getLastError();
        return r;
    }

    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public OutboxEvent.EventStatus getStatus() { return status; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getProcessedAt() { return processedAt; }
    public int getRetryCount() { return retryCount; }
    public String getLastError() { return lastError; }
}