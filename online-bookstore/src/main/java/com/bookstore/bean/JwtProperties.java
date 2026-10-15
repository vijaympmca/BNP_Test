package com.bookstore.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from app.jwt.* in application.yml. */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
