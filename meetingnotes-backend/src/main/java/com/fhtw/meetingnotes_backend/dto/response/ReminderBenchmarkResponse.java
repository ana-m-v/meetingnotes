package com.fhtw.meetingnotes_backend.dto.response;

public class ReminderBenchmarkResponse {

    private int sampleSize;
    private long minMillis;
    private long p50Millis;
    private long p95Millis;
    private long maxMillis;
    private double avgMillis;

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    public long getMinMillis() {
        return minMillis;
    }

    public void setMinMillis(long minMillis) {
        this.minMillis = minMillis;
    }

    public long getP50Millis() {
        return p50Millis;
    }

    public void setP50Millis(long p50Millis) {
        this.p50Millis = p50Millis;
    }

    public long getP95Millis() {
        return p95Millis;
    }

    public void setP95Millis(long p95Millis) {
        this.p95Millis = p95Millis;
    }

    public long getMaxMillis() {
        return maxMillis;
    }

    public void setMaxMillis(long maxMillis) {
        this.maxMillis = maxMillis;
    }

    public double getAvgMillis() {
        return avgMillis;
    }

    public void setAvgMillis(double avgMillis) {
        this.avgMillis = avgMillis;
    }
}