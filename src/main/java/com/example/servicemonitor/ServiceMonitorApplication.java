package com.example.servicemonitor;

import com.example.servicemonitor.config.AppMonitoringProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppMonitoringProperties.class)
public class ServiceMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceMonitorApplication.class, args);
    }
}
