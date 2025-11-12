package com.smartjob.user.util;

import com.smartjob.user.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Clase para generar y validar tokens JWT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    /**
     * Generates a JWT token for a user.
     *
     * @param userId the user's UUID
     * @param email  the user's email
     * @return the generated JWT token
     */
    public String generateToken(UUID userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);

        return createToken(claims, email);
    }

    /**
     * Creates a JWT token using the specified claims.
     *
     * @param claims  the set of claims to include in the token
     * @param subject the subject associated with the token
     * @return the generated JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtProperties.getExpiration());

        log.debug("Generando token JWT para: {}", subject);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();

    }

    /**
     * Extracts the subject (email) from a JWT token.
     *
     * @param token the JWT token
     * @return the email extracted from the token
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the userId from a JWT token.
     *
     * @param token the JWT token
     * @return the user's UUID
     */
    public UUID extractUserId(String token) {
        String userIdStr = (String) extractAllClaims(token).get("userId");
        return UUID.fromString(userIdStr);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims contained in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks whether a JWT token has expired.
     *
     * @param token the JWT token
     * @return true if the token has expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.warn("Error al verificar expiración del token: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token
     * @param email the user's email to validate against
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            return (tokenEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error al validar token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the signing key for JWT tokens.
     * Converts the secret string into a SecretKey.
     *
     * @return the signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}