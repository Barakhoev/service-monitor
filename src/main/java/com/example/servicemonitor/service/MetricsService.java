package com.example.servicemonitor.service;

import com.example.servicemonitor.domain.ErrorType;
import com.example.servicemonitor.domain.HealthCheck;
import com.example.servicemonitor.domain.ServiceStatus;
import com.example.servicemonitor.dto.AvailabilityResponse;
import com.example.servicemonitor.dto.OutagePeriodResponse;
import com.example.servicemonitor.dto.OutagesResponse;
import com.example.servicemonitor.exception.NotFoundException;
import com.example.servicemonitor.repository.HealthCheckRepository;
import com.example.servicemonitor.repository.MonitoredAppRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private final MonitoredAppRepository appRepository;
    private final HealthCheckRepository checkRepository;

    public MetricsService(MonitoredAppRepository appRepository, HealthCheckRepository checkRepository) {
        this.appRepository = appRepository;
        this.checkRepository = checkRepository;
    }

    public AvailabilityResponse availability(Long appId, Instant from, Instant to) {
        ensureAppExists(appId);
        var checks = checkRepository.findByAppIdAndCheckedAtBetweenOrderByCheckedAtAsc(appId, from, to);

        long dataPoints = checks.size();
        long up = checks.stream().filter(c -> c.getStatus() == ServiceStatus.UP).count();
        long down = dataPoints - up;

        Double percent = null;
        if (dataPoints > 0) {
            percent = (up * 100.0) / dataPoints;
        }

        Map<String, Long> downByError = checks.stream()
            .filter(c -> c.getStatus() != ServiceStatus.UP)
            .collect(Collectors.groupingBy(c -> c.getErrorType().name(), Collectors.counting()));

        return new AvailabilityResponse(appId, from, to, dataPoints, up, down, percent, downByError);
    }

    public OutagesResponse outages(Long appId, Instant from, Instant to) {
        ensureAppExists(appId);

        List<HealthCheck> checksInRange = checkRepository.findByAppIdAndCheckedAtBetweenOrderByCheckedAtAsc(appId, from, to);
        Optional<HealthCheck> lastBeforeOpt = checkRepository.findTopByAppIdAndCheckedAtLessThanOrderByCheckedAtDesc(appId, from);

        boolean inOutage = lastBeforeOpt.map(h -> h.getStatus() != ServiceStatus.UP).orElse(false);
        Instant outageStart = inOutage ? from : null;

        long outageChecksCount = 0;
        Map<String, Long> outageErrorCounts = new LinkedHashMap<>();

        List<OutagePeriodResponse> outages = new ArrayList<>();

        // If there are no datapoints in range, we still may have an outage spanning the whole range
        if (checksInRange.isEmpty()) {
            if (inOutage) {
                outages.add(buildOutage(outageStart, to, 0, Map.of()));
            }
            return new OutagesResponse(appId, from, to, outages);
        }

        for (HealthCheck c : checksInRange) {
            boolean isDown = c.getStatus() != ServiceStatus.UP;

            if (!inOutage && isDown) {
                inOutage = true;
                outageStart = c.getCheckedAt();
                outageChecksCount = 0;
                outageErrorCounts = new LinkedHashMap<>();
            }

            if (inOutage) {
                if (isDown) {
                    outageChecksCount++;
                    String key = c.getErrorType() == null ? ErrorType.UNKNOWN_ERROR.name() : c.getErrorType().name();
                    outageErrorCounts.put(key, outageErrorCounts.getOrDefault(key, 0L) + 1);
                } else {
                    // recovered
                    Instant outageEnd = c.getCheckedAt();
                    outages.add(buildOutage(outageStart, outageEnd, outageChecksCount, outageErrorCounts));
                    inOutage = false;
                    outageStart = null;
                }
            }
        }

        if (inOutage && outageStart != null) {
            outages.add(buildOutage(outageStart, to, outageChecksCount, outageErrorCounts));
        }

        return new OutagesResponse(appId, from, to, outages);
    }

    private OutagePeriodResponse buildOutage(Instant start, Instant end, long checksCount, Map<String, Long> errorCounts) {
        long durationSeconds = Math.max(0, Duration.between(start, end).getSeconds());
        Map<String, Long> safeCounts = (errorCounts == null) ? Map.of() : new LinkedHashMap<>(errorCounts);
        return new OutagePeriodResponse(start, end, durationSeconds, checksCount, safeCounts);
    }

    private void ensureAppExists(Long appId) {
        if (!appRepository.existsById(appId)) {
            throw new NotFoundException("App not found: " + appId);
        }
    }
}
