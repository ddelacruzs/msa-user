package com.smartjob.user.repository;

import com.smartjob.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

/**
 * Reactive wrapper for UserRepository.
 * Converts blocking JPA operations into reactive ones using Schedulers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveUserRepository {

    private final UserRepository userRepository;

    /**
     * Saves a user reactively.
     *
     * @param user the user to save
     * @return Mono containing the saved user
     */
    public Mono<UserEntity> save(UserEntity user) {
        log.debug("Guardando usuario con email: {}", user.getEmail());
        return Mono.fromCallable(() -> userRepository.save(user))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(savedUser -> log.debug("Usuario guardado con ID: {}", savedUser.getId()))
                .doOnError(error -> log.error("Error al guardar usuario: {}", error.getMessage()));
    }

    /**
     * Finds a user by email reactively.
     *
     * @param email the user's email
     * @return Mono containing the user if found, or Mono.empty() if not
     */
    public Mono<UserEntity> findByEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        return Mono.fromCallable(() -> userRepository.findByEmail(email))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("Usuario encontrado: {}", user.getId());
                    } else {
                        log.debug("Usuario no encontrado con email: {}", email);
                    }
                });
    }

    /**
     * Finds a user by email with their phones loaded, reactively.
     *
     * @param email the user's email
     * @return Mono containing the user and their phones if found
     */
    public Mono<UserEntity> findByEmailWithPhones(String email) {
        log.debug("Buscando usuario con teléfonos por email: {}", email);
        return Mono.fromCallable(() -> userRepository.findByEmailWithPhones(email))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty);
    }

    /**
     * Checks whether a user with the specified email exists, reactively.
     *
     * @param email the email to check
     * @return Mono<Boolean> with true if the user exists, false otherwise
     */

    public Mono<Boolean> existsByEmail(String email) {
        log.debug("Verificando existencia de email: {}", email);
        return Mono.fromCallable(() -> userRepository.existsByEmail(email))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(exists -> log.debug("Email {} existe: {}", email, exists));
    }

    /**
     * Finds a user by ID reactively.
     *
     * @param id the user's UUID
     * @return Mono containing the user if found
     */

    public Mono<UserEntity> findById(@NonNull UUID id) {
        log.debug("Buscando usuario por ID: {}", id);
        return Mono.fromCallable(() -> userRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty);
    }

    /**
     * Finds all users reactively.
     *
     * @return Flux containing all users
     */

    public Flux<UserEntity> findAll() {
        log.debug("Buscando todos los usuarios");
        return Mono.fromCallable(userRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    /**
     * Deletes a user by ID reactively.
     *
     * @param id the UUID of the user to delete
     * @return Mono<Void> that completes when the operation finishes
     */

    public Mono<Void> deleteById(@NonNull UUID id) {
        log.debug("Eliminando usuario con ID: {}", id);
        return Mono.fromRunnable(() -> userRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then()
                .doOnSuccess(unused -> log.debug("Usuario eliminado: {}", id));
    }

}
