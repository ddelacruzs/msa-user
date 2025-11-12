package com.smartjob.user.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Configuration validator that runs at application startup.
 * Ensures that all validation properties are correctly configured.
 *
 * If any property is missing or has an invalid format, the application will not
 * start.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationConfigValidator {

    private final ValidationProperties validationProperties;

    /**
     * Validates the configuration at application startup.
     * Automatically executed after the bean is constructed.
     */
    @PostConstruct
    public void validateConfiguration() {
        log.info("Validando configuración de patrones de validación...");

        validateEmailConfiguration();
        validatePasswordConfiguration();

        log.info("✅ Configuración de validaciones correcta");
    }

    private void validateEmailConfiguration() {
        validateRule("email", validationProperties.getEmail());
    }

    private void validatePasswordConfiguration() {
        validateRule("password", validationProperties.getPassword());
    }

    /**
     * Validates a generic validation rule.
     * Reusable for any type of validation.
     *
     * @param ruleName the name of the rule (for error messages)
     * @param rule     the rule to validate
     */
    private void validateRule(String ruleName, ValidationProperties.ValidationRule rule) {
        // Validar que el patrón esté configurado
        if (!StringUtils.hasText(rule.getPattern())) {
            throw new IllegalStateException(
                    String.format("La propiedad 'validation.%s.pattern' no está configurada en application.yaml",
                            ruleName));
        }

        if (!StringUtils.hasText(rule.getMessage())) {
            throw new IllegalStateException(
                    String.format("La propiedad 'validation.%s.message' no está configurada en application.yaml",
                            ruleName));
        }

        try {
            Pattern.compile(rule.getPattern());
            log.info("✓ Patrón de {} configurado: {}", ruleName, rule.getPattern());
        } catch (PatternSyntaxException e) {
            throw new IllegalStateException(
                    String.format("El patrón regex de %s es inválido: %s", ruleName, rule.getPattern()), e);
        }
    }
}