package com.fhtw.meetingnotes_backend.dto.request;

public class ParseBatchBenchmarkRequest {

    private int noteCount = 100;
    private int actionsPerNote = 5;
    private int noiseLinesPerNote = 10;
    private boolean uniqueNotes = true;
    private long timeoutSeconds = 60;

    public int getNoteCount() {
        return noteCount;
    }

    public void setNoteCount(int noteCount) {
        this.noteCount = noteCount;
    }

    public int getActionsPerNote() {
        return actionsPerNote;
    }

    public void setActionsPerNote(int actionsPerNote) {
        this.actionsPerNote = actionsPerNote;
    }

    public int getNoiseLinesPerNote() {
        return noiseLinesPerNote;
    }

    public void setNoiseLinesPerNote(int noiseLinesPerNote) {
        this.noiseLinesPerNote = noiseLinesPerNote;
    }

    public boolean isUniqueNotes() {
        return uniqueNotes;
    }

    public void setUniqueNotes(boolean uniqueNotes) {
        this.uniqueNotes = uniqueNotes;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}