package com.example.servicemonitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.monitoring")
public class AppMonitoringProperties {

    /**
     * Spring cron with seconds field (6 parts).
     * Default: every minute.
     */
    private String cron = "0 * * * * *";

    private Duration requestTimeout = Duration.ofSeconds(5);
    private Duration connectTimeout = Duration.ofSeconds(3);

    private String userAgent = "service-monitor/0.0.1";

    /**
     * Max response body bytes stored into DB (rawBody).
     */
    private int maxBodyBytes = 4096;

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getMaxBodyBytes() {
        return maxBodyBytes;
    }

    public void setMaxBodyBytes(int maxBodyBytes) {
        this.maxBodyBytes = maxBodyBytes;
    }
}
