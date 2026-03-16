package com.fhtw.meetingnotes_backend.service.events;

import com.fhtw.meetingnotes_backend.concurrency.ParseExecutor;
import com.fhtw.meetingnotes_backend.service.MeetingNoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class NoteIngestedListener {

    private static final Logger log = LoggerFactory.getLogger(NoteIngestedListener.class);

    private final ParseExecutor parseExecutor;
    private final MeetingNoteService meetingNoteService;

    public NoteIngestedListener(ParseExecutor parseExecutor, MeetingNoteService meetingNoteService) {
        this.parseExecutor = parseExecutor;
        this.meetingNoteService = meetingNoteService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NoteIngestedEvent event) {
        long noteId = event.getNoteId();
        String rawContent = event.getRawContent();

        parseExecutor.submitAsync(noteId, rawContent)
                .thenAccept(items -> meetingNoteService.persistParsedItems(noteId, items))
                .exceptionally(ex -> {
                    log.error("Parse failed for noteId={}", noteId, ex);
                    meetingNoteService.markFailed(noteId);
                    return null;
                });
    }
}
