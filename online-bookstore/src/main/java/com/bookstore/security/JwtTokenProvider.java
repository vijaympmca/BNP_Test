package com.bookstore.security;

import com.bookstore.bean.JwtProperties;
import com.bookstore.pojo.JwtClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Creates and validates HS256-signed JWTs. */
@Component
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(JwtProperties properties) {
        if (properties.secret() == null || properties.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 characters (256 bits)");
        }
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.expirationMs();
    }

    public String generateToken(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("^ROLE_", ""))
                .findFirst()
                .orElse("USER");
        Date now = new Date();
        return Jwts.builder()
                .subject(authentication.getName())
                .claim(ROLE_CLAIM, role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Parses and verifies the token.
     *
     * @throws JwtException             if the signature is invalid, the token is expired or malformed
     * @throws IllegalArgumentException if the token is empty
     */
    public JwtClaims parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new JwtClaims(
                claims.getSubject(),
                claims.get(ROLE_CLAIM, String.class),
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
