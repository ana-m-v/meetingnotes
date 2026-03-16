package com.fhtw.meetingnotes_backend.dto.response;

import com.fhtw.meetingnotes_backend.domain.MeetingNote;
import java.time.Instant;

public class MeetingNoteResponse {

    private Long id;
    private String title;
    private String contentHash;
    private MeetingNote.ParseStatus parseStatus;
    private Instant uploadedAt;
    private Instant parsedAt;
    private int actionItemCount;
    private Long version;

    public static MeetingNoteResponse from(MeetingNote n) {
        MeetingNoteResponse r = new MeetingNoteResponse();
        r.id = n.getId();
        r.title = n.getTitle();
        r.contentHash = n.getContentHash();
        r.parseStatus = n.getParseStatus();
        r.uploadedAt = n.getUploadedAt();
        r.parsedAt = n.getParsedAt();
        r.actionItemCount = n.getActionItems().size();
        r.version = n.getVersion();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContentHash() { return contentHash; }
    public MeetingNote.ParseStatus getParseStatus() { return parseStatus; }
    public Instant getUploadedAt() { return uploadedAt; }
    public Instant getParsedAt() { return parsedAt; }
    public int getActionItemCount() { return actionItemCount; }
    public Long getVersion() { return version; }
}