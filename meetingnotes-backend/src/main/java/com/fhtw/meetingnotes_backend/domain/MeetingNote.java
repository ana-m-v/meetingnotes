package com.fhtw.meetingnotes_backend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "meeting_notes",
        uniqueConstraints = @UniqueConstraint(columnNames = "content_hash"))
public class MeetingNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String contentHash;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawContent;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParseStatus parseStatus = ParseStatus.PENDING;

    @Column
    private Instant uploadedAt = Instant.now();

    @Column
    private Instant parsedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "meetingNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionItem> actionItems = new ArrayList<>();

    public MeetingNote() {}

    public enum ParseStatus {
        PENDING, IN_PROGRESS, DONE, FAILED
    }

    public Long getId() { return id; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public String getRawContent() { return rawContent; }
    public void setRawContent(String rawContent) { this.rawContent = rawContent; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ParseStatus getParseStatus() { return parseStatus; }
    public void setParseStatus(ParseStatus parseStatus) { this.parseStatus = parseStatus; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }

    public Instant getParsedAt() { return parsedAt; }
    public void setParsedAt(Instant parsedAt) { this.parsedAt = parsedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<ActionItem> getActionItems() { return actionItems; }
    public void setActionItems(List<ActionItem> actionItems) { this.actionItems = actionItems; }
}
