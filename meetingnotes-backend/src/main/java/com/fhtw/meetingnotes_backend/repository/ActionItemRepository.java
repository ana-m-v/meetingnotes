package com.fhtw.meetingnotes_backend.repository;

import com.fhtw.meetingnotes_backend.domain.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {
    List<ActionItem> findByMeetingNoteId(Long meetingNoteId);
}