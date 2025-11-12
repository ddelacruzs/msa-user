package com.smartjob.user.service.impl;

import com.smartjob.user.dto.Phone;
import com.smartjob.user.dto.PostUserRequest;
import com.smartjob.user.dto.PostUserResponse;
import com.smartjob.user.entity.PhoneEntity;
import com.smartjob.user.entity.UserEntity;
import com.smartjob.user.exception.EmailAlreadyExistsException;
import com.smartjob.user.repository.ReactiveUserRepository;
import com.smartjob.user.service.util.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserService Integration Tests")
class UserServiceImplTest {

    @MockitoBean
    private ReactiveUserRepository reactiveUserRepository;

    @MockitoBean
    private ValidationService validationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserServiceImpl userService;

    private PostUserRequest validRequest;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        // Setup request
        Phone phone = new Phone();
        phone.setNumber("1234567");
        phone.setCityCode("1");
        phone.setCountryCode("57");

        validRequest = new PostUserRequest();
        validRequest.setName("Juan Rodriguez");
        validRequest.setEmail("juan@rodriguez.org");
        validRequest.setPassword("Hunter2!");
        validRequest.setPhones(List.of(phone));

        // Setup entity
        PhoneEntity phoneEntity = new PhoneEntity();
        phoneEntity.setNumber("1234567");
        phoneEntity.setCityCode("1");
        phoneEntity.setCountryCode("57");

        userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setName("Juan Rodriguez");
        userEntity.setEmail("juan@rodriguez.org");
        userEntity.setPassword("encryptedPassword");
        userEntity.setPhones(List.of(phoneEntity));
        userEntity.setCreated(LocalDateTime.now());
        userEntity.setModified(LocalDateTime.now());
        userEntity.setLastLogin(LocalDateTime.now());
        userEntity.setIsActive(true);
    }

    @Test
    @DisplayName("Debe crear usuario exitosamente")
    void shouldCreateUserSuccessfully() {
        // Given
        when(validationService.validateEmailAndPassword(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(reactiveUserRepository.existsByEmail(anyString()))
                .thenReturn(Mono.just(false));
        when(reactiveUserRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity savedUser = invocation.getArgument(0);
                    LocalDateTime now = LocalDateTime.now();
                    savedUser.setCreated(now);
                    savedUser.setModified(now);
                    savedUser.setLastLogin(now);
                    return Mono.just(savedUser);
                });

        // When
        Mono<PostUserResponse> result = userService.createUser(validRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getId() != null &&
                        response.getToken() != null &&
                        response.getIsActive() != null &&
                        response.getIsActive().equals(true) &&
                        response.getCreated() != null &&
                        response.getModified() != null &&
                        response.getLastLogin() != null)
                .verifyComplete();

        verify(validationService).validateEmailAndPassword(validRequest.getEmail(), validRequest.getPassword());
        verify(reactiveUserRepository).existsByEmail(validRequest.getEmail());
        verify(reactiveUserRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el email ya existe")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(validationService.validateEmailAndPassword(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(reactiveUserRepository.existsByEmail(anyString()))
                .thenReturn(Mono.just(true));

        // When
        Mono<PostUserResponse> result = userService.createUser(validRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof EmailAlreadyExistsException &&
                        throwable.getMessage().equals("El correo ya está registrado"))
                .verify();

        verify(validationService).validateEmailAndPassword(validRequest.getEmail(), validRequest.getPassword());
        verify(reactiveUserRepository).existsByEmail(validRequest.getEmail());
        verify(reactiveUserRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Debe encriptar el password antes de guardar")
    void shouldEncryptPasswordBeforeSaving() {
        // Given
        when(validationService.validateEmailAndPassword(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(reactiveUserRepository.existsByEmail(anyString()))
                .thenReturn(Mono.just(false));
        when(reactiveUserRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity savedUser = invocation.getArgument(0);
                    assert !savedUser.getPassword().equals(validRequest.getPassword());
                    return Mono.just(savedUser);
                });

        // When
        Mono<PostUserResponse> result = userService.createUser(validRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getId() != null)
                .verifyComplete();

        verify(reactiveUserRepository).save(argThat(user -> !user.getPassword().equals(validRequest.getPassword())));
    }

    @Test
    @DisplayName("Debe generar token JWT al crear usuario")
    void shouldGenerateJwtTokenWhenCreatingUser() {
        // Given
        when(validationService.validateEmailAndPassword(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(reactiveUserRepository.existsByEmail(anyString()))
                .thenReturn(Mono.just(false));
        when(reactiveUserRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When
        Mono<PostUserResponse> result = userService.createUser(validRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getToken() != null &&
                        !response.getToken().isEmpty() &&
                        response.getToken().startsWith("eyJ"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe establecer relación bidireccional con teléfonos")
    void shouldEstablishBidirectionalRelationshipWithPhones() {
        // Given
        when(validationService.validateEmailAndPassword(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(reactiveUserRepository.existsByEmail(anyString()))
                .thenReturn(Mono.just(false));
        when(reactiveUserRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity savedUser = invocation.getArgument(0);
                    if (savedUser.getPhones() != null) {
                        for (PhoneEntity phone : savedUser.getPhones()) {
                            assert phone.getUser() != null;
                            assert phone.getUser().equals(savedUser);
                        }
                    }
                    return Mono.just(savedUser);
                });

        // When
        Mono<PostUserResponse> result = userService.createUser(validRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getId() != null)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe asignar UUID al usuario antes de guardar")
    void shouldAssignUuidToUserBeforeSaving() {
        // Given
        when(validationService.validateEmailAndPassword(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(reactiveUserRepository.existsByEmail(anyString()))
                .thenReturn(Mono.just(false));
        when(reactiveUserRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity savedUser = invocation.getArgument(0);
                    assert savedUser.getId() != null;
                    return Mono.just(savedUser);
                });

        // When
        Mono<PostUserResponse> result = userService.createUser(validRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getId() != null)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar usuario sin teléfonos")
    void shouldHandleUserWithoutPhones() {
        // Given
        PostUserRequest requestWithoutPhones = new PostUserRequest();
        requestWithoutPhones.setName("Juan Rodriguez");
        requestWithoutPhones.setEmail("juan@rodriguez.org");
        requestWithoutPhones.setPassword("Hunter2!");
        requestWithoutPhones.setPhones(null);

        when(validationService.validateEmailAndPassword(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(reactiveUserRepository.existsByEmail(anyString()))
                .thenReturn(Mono.just(false));
        when(reactiveUserRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When
        Mono<PostUserResponse> result = userService.createUser(requestWithoutPhones);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getId() != null)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe crear múltiples usuarios con emails diferentes")
    void shouldCreateMultipleUsersWithDifferentEmails() {
        // Given
        PostUserRequest request1 = new PostUserRequest();
        request1.setName("User 1");
        request1.setEmail("user1@example.com");
        request1.setPassword("Password1!");
        request1.setPhones(List.of());

        PostUserRequest request2 = new PostUserRequest();
        request2.setName("User 2");
        request2.setEmail("user2@example.com");
        request2.setPassword("Password2!");
        request2.setPhones(List.of());

        when(validationService.validateEmailAndPassword(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(reactiveUserRepository.existsByEmail(anyString()))
                .thenReturn(Mono.just(false));
        when(reactiveUserRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When
        Mono<PostUserResponse> result1 = userService.createUser(request1);
        Mono<PostUserResponse> result2 = userService.createUser(request2);

        // Then
        StepVerifier.create(result1)
                .expectNextMatches(response -> response.getId() != null &&
                        response.getToken() != null)
                .verifyComplete();

        StepVerifier.create(result2)
                .expectNextMatches(response -> response.getId() != null &&
                        response.getToken() != null)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe propagar errores del repositorio")
    void shouldPropagateRepositoryErrors() {
        // Given
        when(validationService.validateEmailAndPassword(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(reactiveUserRepository.existsByEmail(anyString()))
                .thenReturn(Mono.just(false));
        when(reactiveUserRepository.save(any(UserEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("Error de base de datos")));

        // When
        Mono<PostUserResponse> result = userService.createUser(validRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Error de base de datos"))
                .verify();
    }

    @Test
    @DisplayName("Debe verificar que el password encriptado es válido con BCrypt")
    void shouldVerifyEncryptedPasswordIsValidWithBCrypt() {
        // Given
        when(validationService.validateEmailAndPassword(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(reactiveUserRepository.existsByEmail(anyString()))
                .thenReturn(Mono.just(false));
        when(reactiveUserRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity savedUser = invocation.getArgument(0);
                    boolean matches = passwordEncoder.matches(validRequest.getPassword(), savedUser.getPassword());
                    assert matches : "El password encriptado debe coincidir con el original";
                    return Mono.just(savedUser);
                });

        // When
        Mono<PostUserResponse> result = userService.createUser(validRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getId() != null)
                .verifyComplete();
    }
}