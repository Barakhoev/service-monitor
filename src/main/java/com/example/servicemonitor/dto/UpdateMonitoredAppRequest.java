package com.example.servicemonitor.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateMonitoredAppRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String healthUrl;

    private boolean active = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHealthUrl() {
        return healthUrl;
    }

    public void setHealthUrl(String healthUrl) {
        this.healthUrl = healthUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
