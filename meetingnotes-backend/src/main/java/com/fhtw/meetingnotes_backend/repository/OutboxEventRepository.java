package com.fhtw.meetingnotes_backend.repository;

import com.fhtw.meetingnotes_backend.domain.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' AND e.scheduledAt <= :now ORDER BY e.scheduledAt ASC")
    List<OutboxEvent> findDuePendingEvents(Instant now, Pageable pageable);

    /**
     * Atomic claim — uses optimistic UPDATE with WHERE status = PENDING.
     * Returns 1 if this thread won the race, 0 if another thread already claimed it.
     * This prevents double-dispatch when multiple threads (or app instances) compete.
     */
    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = 'PROCESSING' WHERE e.id = :id AND e.status = 'PENDING'")
    int claimEvent(@Param("id") Long id);

    List<OutboxEvent> findTop50ByOrderByScheduledAtDesc();
}
