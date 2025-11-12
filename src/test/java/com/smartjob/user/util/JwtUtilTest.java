package com.smartjob.user.util;

import com.smartjob.user.config.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("JwtUtil Integration Tests")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    @DisplayName("Debe generar un token JWT válido")
    void shouldGenerateValidToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        // When
        String token = jwtUtil.generateToken(userId, email);

        // Then
        assertNotNull(token, "El token no debe ser null");
        assertFalse(token.isEmpty(), "El token no debe estar vacío");
        assertTrue(token.startsWith("eyJ"), "Los JWT siempre empiezan con 'eyJ'");
    }

    @Test
    @DisplayName("Debe extraer el userId del token")
    void shouldExtractUserIdFromToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtUtil.generateToken(userId, email);

        // When
        UUID extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertNotNull(extractedUserId, "El userId extraído no debe ser null");
        assertEquals(userId, extractedUserId, "El userId extraído debe coincidir con el original");
    }

    @Test
    @DisplayName("Debe extraer el email del token")
    void shouldExtractEmailFromToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtUtil.generateToken(userId, email);

        // When
        String extractedEmail = jwtUtil.extractEmail(token);

        // Then
        assertNotNull(extractedEmail, "El email extraído no debe ser null");
        assertEquals(email, extractedEmail, "El email extraído debe coincidir con el original");
    }

    @Test
    @DisplayName("Debe extraer la fecha de expiración del token")
    void shouldExtractExpirationDateFromToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtUtil.generateToken(userId, email);

        // When
        Date expirationDate = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expirationDate, "La fecha de expiración no debe ser null");
        assertTrue(expirationDate.after(new Date()), "La fecha de expiración debe ser futura");
    }

    @Test
    @DisplayName("Debe validar que el token no ha expirado")
    void shouldValidateTokenNotExpired() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtUtil.generateToken(userId, email);

        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertFalse(isExpired, "El token recién generado no debe estar expirado");
    }

    @Test
    @DisplayName("Debe validar token con email correcto")
    void shouldValidateTokenWithCorrectEmail() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtUtil.generateToken(userId, email);

        // When
        boolean isValid = jwtUtil.validateToken(token, email);

        // Then
        assertTrue(isValid, "El token debe ser válido con el email correcto");
    }

    @Test
    @DisplayName("Debe invalidar token con email incorrecto")
    void shouldInvalidateTokenWithIncorrectEmail() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String wrongEmail = "wrong@example.com";
        String token = jwtUtil.generateToken(userId, email);

        // When
        boolean isValid = jwtUtil.validateToken(token, wrongEmail);

        // Then
        assertFalse(isValid, "El token debe ser inválido con un email diferente");
    }

    @Test
    @DisplayName("Debe generar tokens diferentes para usuarios diferentes")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        // When
        String token1 = jwtUtil.generateToken(userId1, email1);
        String token2 = jwtUtil.generateToken(userId2, email2);

        // Then
        assertNotEquals(token1, token2, "Los tokens deben ser diferentes para usuarios diferentes");
    }

    @Test
    @DisplayName("Debe generar tokens diferentes en diferentes momentos para el mismo usuario")
    void shouldGenerateDifferentTokensForSameUserAtDifferentTimes() throws InterruptedException {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        // When
        String token1 = jwtUtil.generateToken(userId, email);
        Thread.sleep(1000); // Esperar 1 segundo
        String token2 = jwtUtil.generateToken(userId, email);

        // Then
        assertNotEquals(token1, token2,
                "Los tokens generados en diferentes momentos deben ser diferentes (debido a timestamps)");
    }

    @Test
    @DisplayName("Debe extraer todos los claims del token")
    void shouldExtractAllClaimsFromToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtUtil.generateToken(userId, email);

        // When
        // Usamos reflexión para acceder al método privado extractAllClaims
        // O mejor aún, lo testeamos indirectamente a través de los métodos públicos
        String extractedEmail = jwtUtil.extractEmail(token);
        UUID extractedUserId = jwtUtil.extractUserId(token);
        Date expirationDate = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(extractedEmail, "El email debe estar en los claims");
        assertNotNull(extractedUserId, "El userId debe estar en los claims");
        assertNotNull(expirationDate, "La fecha de expiración debe estar en los claims");
        assertEquals(email, extractedEmail, "El email en los claims debe coincidir");
        assertEquals(userId, extractedUserId, "El userId en los claims debe coincidir");
    }

    @Test
    @DisplayName("Debe manejar token inválido al validar")
    void shouldHandleInvalidTokenWhenValidating() {
        // Given
        String invalidToken = "token-invalido-xyz123";
        String email = "test@example.com";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken, email);

        // Then
        assertFalse(isValid, "Un token inválido debe retornar false");
    }

    @Test
    @DisplayName("Debe manejar token inválido al verificar expiración")
    void shouldHandleInvalidTokenWhenCheckingExpiration() {
        // Given
        String invalidToken = "token-invalido-xyz123";

        // When
        boolean isExpired = jwtUtil.isTokenExpired(invalidToken);

        // Then
        assertTrue(isExpired, "Un token inválido debe considerarse expirado");
    }

    @Test
    @DisplayName("Debe usar la configuración de JWT desde application.yaml")
    void shouldUseJwtConfigurationFromApplicationYaml() {
        // Then - Verificar que se cargó la configuración correctamente
        assertNotNull(jwtProperties.getSecret(), "El secret debe estar configurado");
        assertNotNull(jwtProperties.getExpiration(), "El tiempo de expiración debe estar configurado");
        assertTrue(jwtProperties.getSecret().length() >= 32,
                "El secret debe tener al menos 32 caracteres para HS256");
        assertTrue(jwtProperties.getExpiration() > 0,
                "El tiempo de expiración debe ser positivo");
    }

    @Test
    @DisplayName("Debe generar token con tiempo de expiración configurado")
    void shouldGenerateTokenWithConfiguredExpiration() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        long currentTime = System.currentTimeMillis();

        // When
        String token = jwtUtil.generateToken(userId, email);
        Date expirationDate = jwtUtil.extractExpiration(token);

        long tokenExpirationTime = expirationDate.getTime();
        long expectedExpiration = currentTime + jwtProperties.getExpiration();

        // Then
        // Permitimos una diferencia de 5 segundos debido al tiempo de procesamiento
        long difference = Math.abs(tokenExpirationTime - expectedExpiration);
        assertTrue(difference < 5000,
                "El tiempo de expiración debe estar cerca del configurado (diferencia: " + difference + "ms)");
    }

    @Test
    @DisplayName("Debe preservar userId como UUID válido en el token")
    void shouldPreserveUserIdAsValidUUID() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        // When
        String token = jwtUtil.generateToken(userId, email);
        UUID extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertNotNull(extractedUserId, "El userId extraído no debe ser null");
        assertEquals(userId.toString(), extractedUserId.toString(),
                "El UUID debe preservarse exactamente igual");
    }

    @Test
    @DisplayName("Debe generar tokens válidos para múltiples usuarios secuencialmente")
    void shouldGenerateValidTokensForMultipleUsersSequentially() {
        // Given
        int numberOfUsers = 10;

        // When & Then
        for (int i = 0; i < numberOfUsers; i++) {
            UUID userId = UUID.randomUUID();
            String email = "user" + i + "@example.com";

            String token = jwtUtil.generateToken(userId, email);

            assertNotNull(token, "Token " + i + " no debe ser null");
            assertTrue(jwtUtil.validateToken(token, email),
                    "Token " + i + " debe ser válido");
            assertEquals(userId, jwtUtil.extractUserId(token),
                    "UserId debe coincidir para token " + i);
            assertEquals(email, jwtUtil.extractEmail(token),
                    "Email debe coincidir para token " + i);
        }
    }
}