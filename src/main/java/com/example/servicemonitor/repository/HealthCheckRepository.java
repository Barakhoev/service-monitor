package com.example.servicemonitor.repository;

import com.example.servicemonitor.domain.HealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {
    List<HealthCheck> findByAppIdAndCheckedAtBetweenOrderByCheckedAtAsc(Long appId, Instant from, Instant to);

    Optional<HealthCheck> findTopByAppIdAndCheckedAtLessThanOrderByCheckedAtDesc(Long appId, Instant before);
}
