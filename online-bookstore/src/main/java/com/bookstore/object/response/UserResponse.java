package com.bookstore.object.response;

import java.time.LocalDateTime;

/** Public view of a user — the password hash is never included. */
public record UserResponse(
        Long id,
        String fullName,
        String username,
        String email,
        String phone,
        String role,
        String roleName,
        boolean enabled,
        LocalDateTime createdAt) {
}
