package com.example.servicemonitor.dto;

import java.time.Instant;
import java.util.Map;

public class AvailabilityResponse {

    private Long appId;
    private Instant from;
    private Instant to;

    private long dataPoints;
    private long upCount;
    private long downCount;

    /**
     * Null when dataPoints == 0.
     */
    private Double availabilityPercent;

    /**
     * Breakdown of DOWN results by errorType (including NONE when status=DOWN returned by service).
     */
    private Map<String, Long> downByErrorType;

    public AvailabilityResponse() {}

    public AvailabilityResponse(Long appId, Instant from, Instant to, long dataPoints, long upCount, long downCount,
                                Double availabilityPercent, Map<String, Long> downByErrorType) {
        this.appId = appId;
        this.from = from;
        this.to = to;
        this.dataPoints = dataPoints;
        this.upCount = upCount;
        this.downCount = downCount;
        this.availabilityPercent = availabilityPercent;
        this.downByErrorType = downByErrorType;
    }

    public Long getAppId() {
        return appId;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public long getDataPoints() {
        return dataPoints;
    }

    public long getUpCount() {
        return upCount;
    }

    public long getDownCount() {
        return downCount;
    }

    public Double getAvailabilityPercent() {
        return availabilityPercent;
    }

    public Map<String, Long> getDownByErrorType() {
        return downByErrorType;
    }
}
