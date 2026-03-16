package com.fhtw.meetingnotes_backend.repository;


import com.fhtw.meetingnotes_backend.domain.MeetingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MeetingNoteRepository extends JpaRepository<MeetingNote, Long> {
    // Used for idempotent ingestion: if hash already exists, skip
    Optional<MeetingNote> findByContentHash(String contentHash);
}
