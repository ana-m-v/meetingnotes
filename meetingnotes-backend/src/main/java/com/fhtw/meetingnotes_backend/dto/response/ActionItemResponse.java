package com.fhtw.meetingnotes_backend.dto.response;

import com.fhtw.meetingnotes_backend.domain.ActionItem;
import java.time.Instant;
import java.time.LocalDate;

public class ActionItemResponse {

    private Long id;
    private Long meetingNoteId;
    private String description;
    private String owner;
    private LocalDate dueDate;
    private ActionItem.Status status;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;  // sent to frontend so it can be included in update requests

    public static ActionItemResponse from(ActionItem a) {
        ActionItemResponse r = new ActionItemResponse();
        r.id = a.getId();
        r.meetingNoteId = a.getMeetingNote().getId();
        r.description = a.getDescription();
        r.owner = a.getOwner();
        r.dueDate = a.getDueDate();
        r.status = a.getStatus();
        r.createdAt = a.getCreatedAt();
        r.updatedAt = a.getUpdatedAt();
        r.version = a.getVersion();
        return r;
    }

    public Long getId() { return id; }
    public Long getMeetingNoteId() { return meetingNoteId; }
    public String getDescription() { return description; }
    public String getOwner() { return owner; }
    public LocalDate getDueDate() { return dueDate; }
    public ActionItem.Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}