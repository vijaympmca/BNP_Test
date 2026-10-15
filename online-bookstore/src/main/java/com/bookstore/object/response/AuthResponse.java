package com.bookstore.object.response;

/** Returned by login. Send the token as: Authorization: Bearer &lt;accessToken&gt; */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        String username,
        String role,
        String roleName) {
}
