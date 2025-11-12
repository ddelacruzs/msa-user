package com.smartjob.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartjob.user.entity.UserEntity;

/**
 * JPA repository for the User.
 * Provides CRUD operations and custom queries.
 */

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Finds a user by their email.
     * Useful for validating duplicates and for login.
     *
     * @param email the email of the user to search for
     * @return Optional containing the user if found, or Optional.empty() if not
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Checks whether a user exists with the specified email.
     * More efficient than findByEmail when you only need to verify existence.
     *
     * @param email the email to check
     * @return true if a user with that email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by email along with their phones (eager fetch).
     * Avoids the N+1 problem by loading phones in a single query.
     *
     * @param email the user's email
     * @return Optional containing the user with their phones loaded
     */
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.phones WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithPhones(@Param("email") String email);

    /**
     * Finds a user by their ID along with their phones (eager fetch).
     *
     * @param id the user's UUID
     * @return Optional containing the user with their phones loaded
     */
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.phones WHERE u.id = :id")
    Optional<UserEntity> findByIdWithPhones(@Param("id") UUID id);
}
