package com.smartjob.user.service.util;

import com.smartjob.user.exception.InvalidEmailFormatException;
import com.smartjob.user.exception.InvalidPasswordFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@DisplayName("ValidationService Tests")
class ValidationServiceTest {

    @Autowired
    private ValidationService validationService;

    @Test
    @DisplayName("Debe validar email correcto")
    void shouldValidateCorrectEmail() {
        // Given
        String validEmail = "test@example.com";

        // When
        Mono<Void> result = validationService.validateEmail(validEmail);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe lanzar excepción para email inválido")
    void shouldThrowExceptionForInvalidEmail() {
        // Given
        String invalidEmail = "invalid-email";

        // When
        Mono<Void> result = validationService.validateEmail(invalidEmail);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidEmailFormatException &&
                        throwable.getMessage().equals("El formato del email no es válido"))
                .verify();
    }

    @Test
    @DisplayName("Debe validar email sin dominio como inválido")
    void shouldValidateEmailWithoutDomainAsInvalid() {
        // Given
        String invalidEmail = "test@";

        // When
        Mono<Void> result = validationService.validateEmail(invalidEmail);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidEmailFormatException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción para email sin @")
    void shouldThrowExceptionForEmailWithoutAt() {
        // Given
        String invalidEmail = "testexample.com";

        // When
        Mono<Void> result = validationService.validateEmail(invalidEmail);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidEmailFormatException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción para email vacío")
    void shouldThrowExceptionForEmptyEmail() {
        // Given
        String emptyEmail = "";

        // When
        Mono<Void> result = validationService.validateEmail(emptyEmail);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidEmailFormatException &&
                        throwable.getMessage().equals("El email es obligatorio"))
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción para email null")
    void shouldThrowExceptionForNullEmail() {
        // Given
        String nullEmail = null;

        // When
        Mono<Void> result = validationService.validateEmail(nullEmail);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidEmailFormatException &&
                        throwable.getMessage().equals("El email es obligatorio"))
                .verify();
    }

    @Test
    @DisplayName("Debe validar contraseña correcta")
    void shouldValidateCorrectPassword() {
        // Given
        String validPassword = "Password123!";

        // When
        Mono<Void> result = validationService.validatePassword(validPassword);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe lanzar excepción para contraseña sin mayúscula")
    void shouldThrowExceptionForPasswordWithoutUppercase() {
        // Given
        String invalidPassword = "password123!";

        // When
        Mono<Void> result = validationService.validatePassword(invalidPassword);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidPasswordFormatException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción para contraseña sin minúscula")
    void shouldThrowExceptionForPasswordWithoutLowercase() {
        // Given
        String invalidPassword = "PASSWORD123!";

        // When
        Mono<Void> result = validationService.validatePassword(invalidPassword);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidPasswordFormatException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción para contraseña sin número")
    void shouldThrowExceptionForPasswordWithoutNumber() {
        // Given
        String invalidPassword = "Password!";

        // When
        Mono<Void> result = validationService.validatePassword(invalidPassword);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidPasswordFormatException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción para contraseña sin carácter especial")
    void shouldThrowExceptionForPasswordWithoutSpecialChar() {
        // Given
        String invalidPassword = "Password123";

        // When
        Mono<Void> result = validationService.validatePassword(invalidPassword);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidPasswordFormatException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción para contraseña muy corta")
    void shouldThrowExceptionForShortPassword() {
        // Given
        String invalidPassword = "Pass1!";

        // When
        Mono<Void> result = validationService.validatePassword(invalidPassword);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidPasswordFormatException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción para contraseña vacía")
    void shouldThrowExceptionForEmptyPassword() {
        // Given
        String emptyPassword = "";

        // When
        Mono<Void> result = validationService.validatePassword(emptyPassword);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidPasswordFormatException &&
                        throwable.getMessage().equals("La contraseña es obligatoria"))
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción para contraseña null")
    void shouldThrowExceptionForNullPassword() {
        // Given
        String nullPassword = null;

        // When
        Mono<Void> result = validationService.validatePassword(nullPassword);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidPasswordFormatException &&
                        throwable.getMessage().equals("La contraseña es obligatoria"))
                .verify();
    }

    @Test
    @DisplayName("Debe validar contraseña con diferentes caracteres especiales")
    void shouldValidatePasswordWithDifferentSpecialChars() {
        // Given
        String[] validPasswords = {
                "Password123@",
                "Password123$",
                "Password123%",
                "Password123*",
                "Password123&"
        };

        // When & Then
        for (String password : validPasswords) {
            Mono<Void> result = validationService.validatePassword(password);

            StepVerifier.create(result)
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Debe validar emails con diferentes dominios")
    void shouldValidateEmailsWithDifferentDomains() {
        // Given
        String[] validEmails = {
                "test@example.com",
                "user.name@example.co.uk",
                "user+tag@example.org",
                "user_name@example-domain.com"
        };

        // When & Then
        for (String email : validEmails) {
            Mono<Void> result = validationService.validateEmail(email);

            StepVerifier.create(result)
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Debe validar email y password juntos - caso exitoso")
    void shouldValidateEmailAndPasswordTogether() {
        // Given
        String validEmail = "test@example.com";
        String validPassword = "Password123!";

        // When
        Mono<Void> result = validationService.validateEmailAndPassword(validEmail, validPassword);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe fallar cuando email es inválido en validación conjunta")
    void shouldFailWhenEmailIsInvalidInCombinedValidation() {
        // Given
        String invalidEmail = "invalid-email";
        String validPassword = "Password123!";

        // When
        Mono<Void> result = validationService.validateEmailAndPassword(invalidEmail, validPassword);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidEmailFormatException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar cuando password es inválido en validación conjunta")
    void shouldFailWhenPasswordIsInvalidInCombinedValidation() {
        // Given
        String validEmail = "test@example.com";
        String invalidPassword = "weak";

        // When
        Mono<Void> result = validationService.validateEmailAndPassword(validEmail, invalidPassword);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidPasswordFormatException.class)
                .verify();
    }
}
