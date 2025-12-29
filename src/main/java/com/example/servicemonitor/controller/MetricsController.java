package com.example.servicemonitor.controller;

import com.example.servicemonitor.dto.AvailabilityResponse;
import com.example.servicemonitor.dto.MetricsSummaryResponse;
import com.example.servicemonitor.dto.OutagesResponse;
import com.example.servicemonitor.service.MetricsService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/apps/{id}/availability")
    public AvailabilityResponse availability(@PathVariable("id") Long appId,
                                             @RequestParam("from") String from,
                                             @RequestParam("to") String to) {
        Instant f = parseInstant(from);
        Instant t = parseInstant(to);
        validateRange(f, t);
        return metricsService.availability(appId, f, t);
    }

    @GetMapping("/apps/{id}/outages")
    public OutagesResponse outages(@PathVariable("id") Long appId,
                                   @RequestParam("from") String from,
                                   @RequestParam("to") String to) {
        Instant f = parseInstant(from);
        Instant t = parseInstant(to);
        validateRange(f, t);
        return metricsService.outages(appId, f, t);
    }

    @GetMapping("/apps/{id}/summary")
    public MetricsSummaryResponse summary(@PathVariable("id") Long appId,
                                         @RequestParam("from") String from,
                                         @RequestParam("to") String to) {
        Instant f = parseInstant(from);
        Instant t = parseInstant(to);
        validateRange(f, t);
        return new MetricsSummaryResponse(
            metricsService.availability(appId, f, t),
            metricsService.outages(appId, f, t)
        );
    }

    private Instant parseInstant(String s) {
        try {
            return Instant.parse(s);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid instant '" + s + "'. Use ISO-8601, e.g. 2025-01-01T00:00:00Z");
        }
    }

    private void validateRange(Instant from, Instant to) {
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("'from' must be before 'to'");
        }
    }
}
