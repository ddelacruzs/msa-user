package com.smartjob.user.service.domain;

import com.smartjob.user.dto.PostUserRequest;
import com.smartjob.user.dto.PostUserResponse;
import reactor.core.publisher.Mono;

/**
 * Service interface for user management operations.
 */
public interface UserService {

    /**
     * Creates a new user in the system.
     *
     * @param request the DTO containing the data for the user to be created
     * @return a Mono with the response DTO of the created user
     */
    Mono<PostUserResponse> createUser(PostUserRequest request);
}