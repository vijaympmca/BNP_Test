package com.bookstore.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/** Bound from app.settings.* in application.yml. */
@ConfigurationProperties(prefix = "app.settings")
public record AppProperties(
        @DefaultValue("100") int maxPageSize,
        @DefaultValue("500") long slowRequestThresholdMs,
        @DefaultValue("http://localhost:3000") List<String> corsAllowedOrigins,
        @DefaultValue Seed seed) {

    /** Default accounts created at startup — one per role. */
    public record Seed(
            @DefaultValue("admin") String adminUsername,
            @DefaultValue("admin@bookstore.com") String adminEmail,
            @DefaultValue("Bookstore@2026") String adminPassword,
            @DefaultValue("john_doe") String userUsername,
            @DefaultValue("john.doe@bookstore.com") String userEmail,
            @DefaultValue("Reader@2026") String userPassword) {
    }
}
