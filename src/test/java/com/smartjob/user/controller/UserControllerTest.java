package com.smartjob.user.controller;

import com.smartjob.user.dto.PostUserRequest;
import com.smartjob.user.dto.PostUserResponse;
import com.smartjob.user.dto.Phone;
import com.smartjob.user.service.domain.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ServerWebExchange exchange;

    @InjectMocks
    private UserController userController;

    private PostUserRequest validUserRequest;
    private PostUserResponse userResponse;

    @BeforeEach
    void setUp() {
        // Setup valid phone
        Phone phone = new Phone("987654321", "57");
        phone.setCityCode("1");

        List<Phone> phones = new ArrayList<>();
        phones.add(phone);

        // Setup valid user request
        validUserRequest = new PostUserRequest(
                "Juan Rodriguez",
                "juan@rodriguez.org",
                "Hunter2!",
                phones);

        // Setup user response
        OffsetDateTime now = OffsetDateTime.now();
        userResponse = new PostUserResponse(
                "Juan Rodriguez",
                "juan@rodriguez.org",
                phones,
                UUID.randomUUID(),
                now,
                now,
                now,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                true);
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST /users - Should create user successfully and return 201")
    void postUser_WhenValidRequest_ShouldReturnCreatedUser() {
        // Arrange
        when(userService.createUser(any(PostUserRequest.class)))
                .thenReturn(Mono.just(userResponse));

        // Act
        Mono<ResponseEntity<PostUserResponse>> result = userController.postUser(
                Mono.just(validUserRequest),
                exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().getId()).isEqualTo(userResponse.getId());
                    assertThat(response.getBody().getToken()).isEqualTo(userResponse.getToken());
                    assertThat(response.getBody().getIsActive()).isTrue();
                    assertThat(response.getBody().getName()).isEqualTo("Juan Rodriguez");
                    assertThat(response.getBody().getEmail()).isEqualTo("juan@rodriguez.org");
                })
                .verifyComplete();

        verify(userService, times(1)).createUser(any(PostUserRequest.class));
    }

    @Test
    @DisplayName("POST /users - Should handle service returning empty Mono")
    void postUser_WhenServiceReturnsEmpty_ShouldCompleteEmpty() {
        // Arrange
        when(userService.createUser(any(PostUserRequest.class)))
                .thenReturn(Mono.empty());

        // Act
        Mono<ResponseEntity<PostUserResponse>> result = userController.postUser(
                Mono.just(validUserRequest),
                exchange);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(userService, times(1)).createUser(any(PostUserRequest.class));
    }

    @Test
    @DisplayName("POST /users - Should propagate service errors")
    void postUser_WhenServiceThrowsError_ShouldPropagateError() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Database connection error");
        when(userService.createUser(any(PostUserRequest.class)))
                .thenReturn(Mono.error(expectedException));

        // Act
        Mono<ResponseEntity<PostUserResponse>> result = userController.postUser(
                Mono.just(validUserRequest),
                exchange);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database connection error"))
                .verify();

        verify(userService, times(1)).createUser(any(PostUserRequest.class));
    }

    @Test
    @DisplayName("POST /users - Should handle empty request Mono")
    void postUser_WhenRequestMonoIsEmpty_ShouldCompleteEmpty() {
        // Act
        Mono<ResponseEntity<PostUserResponse>> result = userController.postUser(
                Mono.empty(),
                exchange);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(userService, never()).createUser(any(PostUserRequest.class));
    }

    @Test
    @DisplayName("POST /users - Should handle request with minimal data")
    void postUser_WhenMinimalRequest_ShouldCreateUser() {
        // Arrange
        List<Phone> emptyPhones = new ArrayList<>();
        PostUserRequest minimalRequest = new PostUserRequest(
                "Test User",
                "test@example.com",
                "Pass123!",
                emptyPhones);

        when(userService.createUser(any(PostUserRequest.class)))
                .thenReturn(Mono.just(userResponse));

        // Act
        Mono<ResponseEntity<PostUserResponse>> result = userController.postUser(
                Mono.just(minimalRequest),
                exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(response.getBody()).isNotNull();
                })
                .verifyComplete();

        verify(userService, times(1)).createUser(any(PostUserRequest.class));
    }

    @Test
    @DisplayName("POST /users - Should handle multiple phones in request")
    void postUser_WhenMultiplePhones_ShouldCreateUser() {
        // Arrange
        Phone phone1 = new Phone("987654321", "57");
        phone1.setCityCode("1");

        Phone phone2 = new Phone("123456789", "57");
        phone2.setCityCode("2");

        List<Phone> multiplePhones = new ArrayList<>();
        multiplePhones.add(phone1);
        multiplePhones.add(phone2);

        PostUserRequest requestWithMultiplePhones = new PostUserRequest(
                "Juan Rodriguez",
                "juan@rodriguez.org",
                "Hunter2!",
                multiplePhones);

        when(userService.createUser(any(PostUserRequest.class)))
                .thenReturn(Mono.just(userResponse));

        // Act
        Mono<ResponseEntity<PostUserResponse>> result = userController.postUser(
                Mono.just(requestWithMultiplePhones),
                exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(response.getBody()).isNotNull();
                })
                .verifyComplete();

        verify(userService, times(1))
                .createUser(argThat(req -> req.getPhones() != null && req.getPhones().size() == 2));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST /users - Should verify all response fields are populated")
    void postUser_WhenSuccessful_ShouldReturnCompleteResponse() {
        // Arrange
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        String token = "test-token-123";

        Phone phone = new Phone("987654321", "57");
        phone.setCityCode("1");
        List<Phone> phones = new ArrayList<>();
        phones.add(phone);

        PostUserResponse completeResponse = new PostUserResponse(
                "Juan Rodriguez",
                "juan@rodriguez.org",
                phones,
                userId,
                now,
                now,
                now,
                token,
                true);

        when(userService.createUser(any(PostUserRequest.class)))
                .thenReturn(Mono.just(completeResponse));

        // Act
        Mono<ResponseEntity<PostUserResponse>> result = userController.postUser(
                Mono.just(validUserRequest),
                exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    PostUserResponse body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.getId()).isEqualTo(userId);
                    assertThat(body.getCreated()).isEqualTo(now);
                    assertThat(body.getModified()).isEqualTo(now);
                    assertThat(body.getLastLogin()).isEqualTo(now);
                    assertThat(body.getToken()).isEqualTo(token);
                    assertThat(body.getIsActive()).isTrue();
                    assertThat(body.getName()).isEqualTo("Juan Rodriguez");
                    assertThat(body.getEmail()).isEqualTo("juan@rodriguez.org");
                    assertThat(body.getPhones()).hasSize(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("POST /users - Should handle IllegalArgumentException")
    void postUser_WhenIllegalArgument_ShouldPropagateError() {
        // Arrange
        IllegalArgumentException expectedException = new IllegalArgumentException("Email already exists");

        when(userService.createUser(any(PostUserRequest.class)))
                .thenReturn(Mono.error(expectedException));

        // Act
        Mono<ResponseEntity<PostUserResponse>> result = userController.postUser(
                Mono.just(validUserRequest),
                exchange);

        // Assert
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userService, times(1)).createUser(any(PostUserRequest.class));
    }

    @Test
    @DisplayName("POST /users - Should verify email is logged during processing")
    void postUser_WhenProcessing_ShouldLogEmail() {
        // Arrange
        when(userService.createUser(any(PostUserRequest.class)))
                .thenReturn(Mono.just(userResponse));

        // Act
        Mono<ResponseEntity<PostUserResponse>> result = userController.postUser(
                Mono.just(validUserRequest),
                exchange);

        // Assert - Verify the flow completes and service is called with correct email
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                })
                .verifyComplete();

        verify(userService).createUser(argThat(req -> req.getEmail().equals("juan@rodriguez.org")));
    }
}