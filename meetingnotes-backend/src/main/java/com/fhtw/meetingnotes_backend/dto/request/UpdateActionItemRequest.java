package com.fhtw.meetingnotes_backend.dto.request;

import com.fhtw.meetingnotes_backend.domain.ActionItem;
import java.time.Instant;
import java.time.LocalDate;

public class UpdateActionItemRequest {

    private String owner;
    private LocalDate dueDate;
    private ActionItem.Status status;
    private boolean scheduleReminder;
    private String ownerEmail;
    private Instant reminderAt;
    private Long version;  // client must send back the version they loaded — used for optimistic locking

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public ActionItem.Status getStatus() { return status; }
    public void setStatus(ActionItem.Status status) { this.status = status; }

    public boolean isScheduleReminder() { return scheduleReminder; }
    public void setScheduleReminder(boolean scheduleReminder) { this.scheduleReminder = scheduleReminder; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public Instant getReminderAt() { return reminderAt; }
    public void setReminderAt(Instant reminderAt) { this.reminderAt = reminderAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}