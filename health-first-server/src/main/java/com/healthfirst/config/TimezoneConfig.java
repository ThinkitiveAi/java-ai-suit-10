package com.healthfirst.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import java.time.ZoneOffset;
import java.util.TimeZone;

@Configuration
public class TimezoneConfig {

    /**
     * Set default timezone to UTC for the entire application
     * This ensures all LocalDateTime operations use UTC for global consistency
     */
    @PostConstruct
    public void setDefaultTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    }

    /**
     * Configure Jackson to handle time zone properly for JSON serialization
     */
    @Bean
    @Primary
    public JavaTimeModule javaTimeModule() {
        JavaTimeModule module = new JavaTimeModule();
        return module;
    }
} 