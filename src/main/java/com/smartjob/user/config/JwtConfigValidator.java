package com.smartjob.user.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT configuration validator that runs at application startup.
 * Ensures that the JWT properties are correctly configured.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtConfigValidator {

    private final JwtProperties jwtProperties;

    /**
     * Validates the JWT configuration at application startup.
     */
    @PostConstruct
    public void validateConfiguration() {
        log.info("Validando configuración de JWT...");

        validateSecret();
        validateExpiration();

        log.info("✅ Configuración de JWT correcta");
    }

    /**
     * Validates that the secret is configured and has an adequate length.
     */
    private void validateSecret() {
        if (!StringUtils.hasText(jwtProperties.getSecret())) {
            throw new IllegalStateException(
                    "La propiedad 'jwt.secret' no está configurada en application.yaml");
        }

        if (jwtProperties.getSecret().length() < 32) {
            throw new IllegalStateException(
                    "La propiedad 'jwt.secret' debe tener al menos 32 caracteres para HS256. " +
                            "Longitud actual: " + jwtProperties.getSecret().length());
        }

        log.info("✓ JWT secret configurado (longitud: {} caracteres)", jwtProperties.getSecret().length());
    }

    /**
     * Validates that the expiration time is configured.
     */
    private void validateExpiration() {
        if (jwtProperties.getExpiration() == null) {
            throw new IllegalStateException(
                    "La propiedad 'jwt.expiration' no está configurada en application.yaml");
        }

        if (jwtProperties.getExpiration() <= 0) {
            throw new IllegalStateException(
                    "La propiedad 'jwt.expiration' debe ser mayor a 0");
        }

        long hours = jwtProperties.getExpiration() / (1000 * 60 * 60);
        log.info("✓ JWT expiration configurado: {} horas", hours);
    }
}