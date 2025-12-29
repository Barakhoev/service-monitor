package com.example.servicemonitor.controller;

import com.example.servicemonitor.dto.CreateMonitoredAppRequest;
import com.example.servicemonitor.dto.MonitoredAppResponse;
import com.example.servicemonitor.dto.SetActiveRequest;
import com.example.servicemonitor.dto.UpdateMonitoredAppRequest;
import com.example.servicemonitor.service.MonitoredAppService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/apps")
public class AdminAppsController {

    private final MonitoredAppService service;

    public AdminAppsController(MonitoredAppService service) {
        this.service = service;
    }

    @GetMapping
    public List<MonitoredAppResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public MonitoredAppResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MonitoredAppResponse create(@Valid @RequestBody CreateMonitoredAppRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public MonitoredAppResponse update(@PathVariable Long id, @Valid @RequestBody UpdateMonitoredAppRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/active")
    public MonitoredAppResponse setActive(@PathVariable Long id, @RequestBody SetActiveRequest req) {
        return service.setActive(id, req.isActive());
    }
}
