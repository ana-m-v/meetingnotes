package com.fhtw.meetingnotes_backend.dto.request;

public class ReminderSeedRequest {

    private int count = 100;
    private String ownerEmail = "demo@example.com";

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}