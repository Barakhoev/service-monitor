package com.example.servicemonitor.dto;

import java.time.Instant;
import java.util.Map;

public class OutagePeriodResponse {

    private Instant start;
    private Instant end;
    private long durationSeconds;
    private long checksCount;
    private Map<String, Long> errorTypeCounts;

    public OutagePeriodResponse() {}

    public OutagePeriodResponse(Instant start, Instant end, long durationSeconds, long checksCount, Map<String, Long> errorTypeCounts) {
        this.start = start;
        this.end = end;
        this.durationSeconds = durationSeconds;
        this.checksCount = checksCount;
        this.errorTypeCounts = errorTypeCounts;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public long getChecksCount() {
        return checksCount;
    }

    public Map<String, Long> getErrorTypeCounts() {
        return errorTypeCounts;
    }
}
