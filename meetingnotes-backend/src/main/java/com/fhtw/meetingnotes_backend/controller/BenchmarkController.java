package com.fhtw.meetingnotes_backend.controller;

import com.fhtw.meetingnotes_backend.dto.request.ParseBatchBenchmarkRequest;
import com.fhtw.meetingnotes_backend.dto.request.ReminderSeedRequest;
import com.fhtw.meetingnotes_backend.dto.response.ParseBatchBenchmarkResponse;
import com.fhtw.meetingnotes_backend.dto.response.ReminderBenchmarkResponse;
import com.fhtw.meetingnotes_backend.service.BenchmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/benchmarks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    @PostMapping("/parse-batch")
    public ParseBatchBenchmarkResponse runParseBatch(@RequestBody ParseBatchBenchmarkRequest req) {
        return benchmarkService.runParseBatchBenchmark(req);
    }

    @GetMapping("/reminders")
    public ReminderBenchmarkResponse reminderLatency() {
        return benchmarkService.computeReminderLatencyBenchmark();
    }

    @PostMapping("/reminders/seed")
    public String seedReminders(@RequestBody ReminderSeedRequest req) {
        return benchmarkService.seedReminderEvents(req.getCount(), req.getOwnerEmail());
    }
}