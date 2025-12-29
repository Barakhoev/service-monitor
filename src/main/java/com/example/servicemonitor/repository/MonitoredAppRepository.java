package com.example.servicemonitor.repository;

import com.example.servicemonitor.domain.MonitoredApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitoredAppRepository extends JpaRepository<MonitoredApp, Long> {
    List<MonitoredApp> findByActiveTrue();
}
