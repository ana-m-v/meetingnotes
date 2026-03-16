package com.fhtw.meetingnotes_backend.outbox;

import com.fhtw.meetingnotes_backend.domain.OutboxEvent;
import com.fhtw.meetingnotes_backend.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxEventRepository outboxRepo;
    private final OutboxDispatcher dispatcher;

    @Value("${app.reminder.batch-size:50}")
    private int batchSize;

    private final ExecutorService deliveryPool = Executors.newFixedThreadPool(8);

    @Scheduled(fixedDelay = 5000)
    public void processBatch() {
        List<OutboxEvent> batch = outboxRepo.findDuePendingEvents(
                Instant.now(), PageRequest.of(0, batchSize)
        );
        if (batch.isEmpty()) return;

        log.info("Outbox: found {} candidate events", batch.size());

        // Fan out concurrently - each dispatch runs in its own transaction
        List<CompletableFuture<Void>> futures = batch.stream()
                .map(event -> CompletableFuture.runAsync(
                        () -> dispatcher.dispatch(event.getId()), deliveryPool)
                )
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
