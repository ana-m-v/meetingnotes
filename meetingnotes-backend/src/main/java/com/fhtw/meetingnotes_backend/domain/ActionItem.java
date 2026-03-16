package com.fhtw.meetingnotes_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "action_items")
@Getter @Setter @NoArgsConstructor
public class ActionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_note_id", nullable = false)
    private MeetingNote meetingNote;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column
    private String owner;

    @Column
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant updatedAt;

    @Version
    private Long version;

    public enum Status {
        OPEN, IN_PROGRESS, DONE
    }
}