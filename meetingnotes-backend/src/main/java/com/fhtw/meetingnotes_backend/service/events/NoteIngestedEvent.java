package com.fhtw.meetingnotes_backend.service.events;

public class NoteIngestedEvent {

    private final long noteId;
    private final String rawContent;

    public NoteIngestedEvent(long noteId, String rawContent) {
        this.noteId = noteId;
        this.rawContent = rawContent;
    }

    public long getNoteId() {
        return noteId;
    }

    public String getRawContent() {
        return rawContent;
    }
}
