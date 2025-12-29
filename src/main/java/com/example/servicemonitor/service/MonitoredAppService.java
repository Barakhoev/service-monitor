package com.example.servicemonitor.service;

import com.example.servicemonitor.domain.MonitoredApp;
import com.example.servicemonitor.dto.CreateMonitoredAppRequest;
import com.example.servicemonitor.dto.MonitoredAppResponse;
import com.example.servicemonitor.dto.UpdateMonitoredAppRequest;
import com.example.servicemonitor.exception.NotFoundException;
import com.example.servicemonitor.repository.MonitoredAppRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

@Service
public class MonitoredAppService {

    private final MonitoredAppRepository repository;

    public MonitoredAppService(MonitoredAppRepository repository) {
        this.repository = repository;
    }

    public List<MonitoredAppResponse> list() {
        return repository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public MonitoredAppResponse get(Long id) {
        MonitoredApp app = repository.findById(id).orElseThrow(() -> new NotFoundException("App not found: " + id));
        return toResponse(app);
    }

    @Transactional
    public MonitoredAppResponse create(CreateMonitoredAppRequest req) {
        validateUrl(req.getHealthUrl());
        MonitoredApp app = new MonitoredApp();
        app.setName(req.getName().trim());
        app.setHealthUrl(req.getHealthUrl().trim());
        app.setActive(req.isActive());
        return toResponse(repository.save(app));
    }

    @Transactional
    public MonitoredAppResponse update(Long id, UpdateMonitoredAppRequest req) {
        validateUrl(req.getHealthUrl());
        MonitoredApp app = repository.findById(id).orElseThrow(() -> new NotFoundException("App not found: " + id));
        app.setName(req.getName().trim());
        app.setHealthUrl(req.getHealthUrl().trim());
        app.setActive(req.isActive());
        return toResponse(repository.save(app));
    }

    @Transactional
    public MonitoredAppResponse setActive(Long id, boolean active) {
        MonitoredApp app = repository.findById(id).orElseThrow(() -> new NotFoundException("App not found: " + id));
        app.setActive(active);
        return toResponse(repository.save(app));
    }

    private void validateUrl(String url) {
        try {
            URI u = URI.create(url.trim());
            String scheme = u.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("healthUrl must be http/https URL");
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid healthUrl: " + ex.getMessage());
        }
    }

    private MonitoredAppResponse toResponse(MonitoredApp app) {
        return new MonitoredAppResponse(app.getId(), app.getName(), app.getHealthUrl(), app.isActive(), app.getCreatedAt());
    }
}
