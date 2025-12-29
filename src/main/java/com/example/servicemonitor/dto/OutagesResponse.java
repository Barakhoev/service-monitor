package com.example.servicemonitor.dto;

import java.time.Instant;
import java.util.List;

public class OutagesResponse {

    private Long appId;
    private Instant from;
    private Instant to;
    private long outagesCount;
    private List<OutagePeriodResponse> outages;

    public OutagesResponse() {}

    public OutagesResponse(Long appId, Instant from, Instant to, List<OutagePeriodResponse> outages) {
        this.appId = appId;
        this.from = from;
        this.to = to;
        this.outages = outages;
        this.outagesCount = outages == null ? 0 : outages.size();
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

    public long getOutagesCount() {
        return outagesCount;
    }

    public List<OutagePeriodResponse> getOutages() {
        return outages;
    }
}
