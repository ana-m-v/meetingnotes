package com.fhtw.meetingnotes_backend.dto.response;

public class ParseBatchBenchmarkResponse {

    private int requestedNotes;
    private int createdNotes;
    private int duplicateNotes;
    private int totalExtractedActions;
    private long elapsedMillis;
    private double actionsPerSecond;
    private double notesPerSecond;
    private boolean completedWithinTimeout;

    public int getRequestedNotes() {
        return requestedNotes;
    }

    public void setRequestedNotes(int requestedNotes) {
        this.requestedNotes = requestedNotes;
    }

    public int getCreatedNotes() {
        return createdNotes;
    }

    public void setCreatedNotes(int createdNotes) {
        this.createdNotes = createdNotes;
    }

    public int getDuplicateNotes() {
        return duplicateNotes;
    }

    public void setDuplicateNotes(int duplicateNotes) {
        this.duplicateNotes = duplicateNotes;
    }

    public int getTotalExtractedActions() {
        return totalExtractedActions;
    }

    public void setTotalExtractedActions(int totalExtractedActions) {
        this.totalExtractedActions = totalExtractedActions;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public void setElapsedMillis(long elapsedMillis) {
        this.elapsedMillis = elapsedMillis;
    }

    public double getActionsPerSecond() {
        return actionsPerSecond;
    }

    public void setActionsPerSecond(double actionsPerSecond) {
        this.actionsPerSecond = actionsPerSecond;
    }

    public double getNotesPerSecond() {
        return notesPerSecond;
    }

    public void setNotesPerSecond(double notesPerSecond) {
        this.notesPerSecond = notesPerSecond;
    }

    public boolean isCompletedWithinTimeout() {
        return completedWithinTimeout;
    }

    public void setCompletedWithinTimeout(boolean completedWithinTimeout) {
        this.completedWithinTimeout = completedWithinTimeout;
    }
}