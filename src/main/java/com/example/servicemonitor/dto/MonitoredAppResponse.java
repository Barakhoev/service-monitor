package com.example.servicemonitor.dto;

import java.time.Instant;

public class MonitoredAppResponse {

    private Long id;
    private String name;
    private String healthUrl;
    private boolean active;
    private Instant createdAt;

    public MonitoredAppResponse() {}

    public MonitoredAppResponse(Long id, String name, String healthUrl, boolean active, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.healthUrl = healthUrl;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHealthUrl() {
        return healthUrl;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
