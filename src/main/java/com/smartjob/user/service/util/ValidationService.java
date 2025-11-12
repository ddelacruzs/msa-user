package com.smartjob.user.service.util;

import com.smartjob.user.config.ValidationProperties;
import com.smartjob.user.exception.InvalidEmailFormatException;
import com.smartjob.user.exception.InvalidPasswordFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

/**
 * User data validation service.
 * Uses configurable regex patterns from application.yaml.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationService {

    private final ValidationProperties validationProperties;

    /**
     * Validates the email format according to the configured pattern.
     *
     * @param email the email to validate
     * @return Mono.empty() if valid, Mono.error() otherwise
     */
    public Mono<Void> validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.warn("Intento de validación con email vacío");
            return Mono.error(new InvalidEmailFormatException("El email es obligatorio"));
        }

        String pattern = validationProperties.getEmail().getPattern();
        if (!Pattern.matches(pattern, email)) {
            log.warn("Email inválido: {} - No cumple con el patrón: {}", email, pattern);
            return Mono.error(new InvalidEmailFormatException(
                    validationProperties.getEmail().getMessage()));
        }

        log.debug("Email válido: {}", email);
        return Mono.empty();
    }

    /**
     * Validates the password format according to the configured pattern.
     *
     * @param password the password to validate
     * @return Mono.empty() if valid, Mono.error() otherwise
     */
    public Mono<Void> validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            log.warn("Intento de validación con password vacío");
            return Mono.error(new InvalidPasswordFormatException("La contraseña es obligatoria"));
        }

        String pattern = validationProperties.getPassword().getPattern();
        if (!Pattern.matches(pattern, password)) {
            log.warn("Password inválido - No cumple con el patrón configurable de seguridad");
            return Mono.error(new InvalidPasswordFormatException(
                    validationProperties.getPassword().getMessage()));
        }

        log.debug("Password válido según patrón configurable");
        return Mono.empty();
    }

    /**
     * Validates email and password in a single reactive operation.
     * Composes validations using then() to execute them sequentially.
     *
     * @param email    the user's email
     * @param password the user's password
     * @return Mono.empty() if both validations pass, Mono.error() on the first
     *         failure
     */
    public Mono<Void> validateEmailAndPassword(String email, String password) {
        return validateEmail(email)
                .then(validatePassword(password));
    }

}