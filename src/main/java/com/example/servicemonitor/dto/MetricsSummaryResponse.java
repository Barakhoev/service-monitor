package com.example.servicemonitor.dto;

public class MetricsSummaryResponse {

    private AvailabilityResponse availability;
    private OutagesResponse outages;

    public MetricsSummaryResponse() {}

    public MetricsSummaryResponse(AvailabilityResponse availability, OutagesResponse outages) {
        this.availability = availability;
        this.outages = outages;
    }

    public AvailabilityResponse getAvailability() {
        return availability;
    }

    public OutagesResponse getOutages() {
        return outages;
    }
}
