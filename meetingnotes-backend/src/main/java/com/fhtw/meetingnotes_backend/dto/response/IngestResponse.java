package com.fhtw.meetingnotes_backend.dto.response;

public class IngestResponse {

    private MeetingNoteResponse note;
    private boolean duplicate;

    public MeetingNoteResponse getNote() { return note; }
    public void setNote(MeetingNoteResponse note) { this.note = note; }

    public boolean isDuplicate() { return duplicate; }
    public void setDuplicate(boolean duplicate) { this.duplicate = duplicate; }
}
