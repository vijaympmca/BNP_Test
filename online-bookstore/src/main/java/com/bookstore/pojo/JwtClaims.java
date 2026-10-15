package com.bookstore.pojo;

import java.time.Instant;

/** Plain model of the values read from a validated JWT. */
public record JwtClaims(String username, String role, Instant issuedAt, Instant expiresAt) {
}
