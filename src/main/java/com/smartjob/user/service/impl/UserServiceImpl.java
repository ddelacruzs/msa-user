package com.smartjob.user.service.impl;

import com.smartjob.user.dto.PostUserRequest;
import com.smartjob.user.dto.PostUserResponse;
import com.smartjob.user.entity.PhoneEntity;
import com.smartjob.user.entity.UserEntity;
import com.smartjob.user.exception.EmailAlreadyExistsException;
import com.smartjob.user.mapper.UserMapper;
import com.smartjob.user.repository.ReactiveUserRepository;
import com.smartjob.user.service.domain.UserService;
import com.smartjob.user.service.util.ValidationService;
import com.smartjob.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive implementation of {@link UserService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ValidationService validationService;
    private final ReactiveUserRepository reactiveUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    public Mono<PostUserResponse> createUser(PostUserRequest request) {
        log.info("Iniciando creación de usuario con email: {}", request.getEmail());

        return validationService.validateEmailAndPassword(request.getEmail(), request.getPassword())
                .then(validateEmailNotExists(request.getEmail()))
                .then(Mono.fromCallable(() -> buildUserEntity(request)))
                .flatMap(reactiveUserRepository::save)
                .flatMap(this::generateAndAssignToken)
                .map(userMapper::toResponse)
                .doOnSuccess(response -> log.info("Usuario creado exitosamente con ID: {}", response.getId()))
                .doOnError(error -> log.error("Error al crear usuario: {}", error.getMessage(), error));
    }

    private Mono<Void> validateEmailNotExists(String email) {
        return reactiveUserRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Intento de registro con email existente: {}", email);
                        return Mono.error(new EmailAlreadyExistsException("El correo ya está registrado"));
                    }
                    return Mono.empty();
                });
    }

    private UserEntity buildUserEntity(PostUserRequest request) {
        log.debug("Construyendo entidad de usuario");

        UserEntity user = userMapper.toEntity(request);
        user.setId(UUID.randomUUID());

        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encryptedPassword);
        log.debug("Password encriptado con BCrypt");

        if (user.getPhones() != null) {
            for (PhoneEntity phone : user.getPhones()) {
                phone.setUser(user);
            }
            log.debug("Relación bidireccional establecida con {} teléfono(s)", user.getPhones().size());
        }

        return user;
    }

    private Mono<UserEntity> generateAndAssignToken(UserEntity user) {
        return Mono.fromCallable(() -> {
            log.debug("Generando token JWT para usuario ID: {}", user.getId());
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());
            user.setToken(token);
            log.debug("Token JWT generado y asignado");
            return user;
        });
    }
}