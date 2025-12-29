package com.example.servicemonitor.service;

import com.example.servicemonitor.domain.HealthCheck;
import com.example.servicemonitor.domain.MonitoredApp;
import com.example.servicemonitor.repository.HealthCheckRepository;
import com.example.servicemonitor.repository.MonitoredAppRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class HealthCheckCollector {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckCollector.class);

    private final MonitoredAppRepository appRepository;
    private final HealthCheckRepository checkRepository;
    private final HealthProbeClient probeClient;

    public HealthCheckCollector(MonitoredAppRepository appRepository,
                                HealthCheckRepository checkRepository,
                                HealthProbeClient probeClient) {
        this.appRepository = appRepository;
        this.checkRepository = checkRepository;
        this.probeClient = probeClient;
    }

    @Scheduled(cron = "${app.monitoring.cron}")
    @Transactional
    public void collect() {
        List<MonitoredApp> apps = appRepository.findByActiveTrue();
        if (apps.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        for (MonitoredApp app : apps) {
            try {
                var result = probeClient.probe(app.getHealthUrl());

                HealthCheck hc = new HealthCheck();
                hc.setApp(app);
                hc.setCheckedAt(now);
                hc.setStatus(result.status());
                hc.setHttpStatus(result.httpStatus());
                hc.setErrorType(result.errorType());
                hc.setLatencyMs(result.latencyMs());
                hc.setRawBody(result.rawBody());
                hc.setErrorMessage(result.errorMessage());

                checkRepository.save(hc);
            } catch (Exception e) {
                log.warn("Failed to collect health for appId={} url={}: {}", app.getId(), app.getHealthUrl(), e.getMessage());
            }
        }
    }
}
