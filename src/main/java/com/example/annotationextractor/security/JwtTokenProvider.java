package com.example.annotationextractor.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper for generating and validating JWT tokens.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long validityInMillis;
    private final long refreshValidityInMillis;

    public JwtTokenProvider(
            @Value("${testcraft.security.jwt.secret:change-me-default-secret-change-me-default-secret}") String secret,
            @Value("${testcraft.security.jwt.validity-ms:3600000}") long validityInMillis,
            @Value("${testcraft.security.jwt.refresh-validity-ms:604800000}") long refreshValidityInMillis) {
        // Use the secret directly as bytes for HMAC key generation
        // If secret is Base64-encoded, it should be decoded first, otherwise use UTF-8 bytes
        byte[] keyBytes;
        try {
            // Try to decode as Base64 first (for production secrets)
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            // If not Base64, use the string bytes directly
            keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        // Ensure minimum key length for HS256 (256 bits = 32 bytes)
        if (keyBytes.length < 32) {
            // Pad or extend the key to meet minimum requirements
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.validityInMillis = validityInMillis;
        this.refreshValidityInMillis = refreshValidityInMillis;
    }

    /**
     * Generate a token from an Authentication object (standard login flow)
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return generateToken(username, roles, validityInMillis);
    }

    /**
     * Generate a token for a specific user and roles with default validity
     */
    public String generateToken(String username, List<String> roles) {
        String rolesString = roles != null ? String.join(",", roles) : "";
        return generateToken(username, rolesString, validityInMillis);
    }

    /**
     * Generate a token with custom expiration time
     */
    public String generateToken(String username, String roles, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generate a refresh token (longer validity)
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshValidityInMillis);

        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Check if a token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            Object type = claims.get("type");
            return "refresh".equals(type);
        } catch (Exception ex) {
            return false;
        }
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoles(String token) {
        Object roles = getClaims(token).get("roles");
        return roles != null ? roles.toString() : "";
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            // Don't accept refresh tokens as access tokens
            Object type = claims.get("type");
            if ("refresh".equals(type)) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Validate a refresh token
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            Object type = claims.get("type");
            return "refresh".equals(type);
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}


