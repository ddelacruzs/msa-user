package com.smartjob.user.mapper;

import com.smartjob.user.dto.Error;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert exceptions into Error objects according to the OpenAPI
 * contract.
 */
@Component
public class ErrorMapper {

    /**
     * Creates an Error object from a message.
     *
     * @param message the error message
     * @return Error object following the OpenAPI specification
     */
    public Error toError(String message) {
        Error error = new Error();
        error.setMessage(truncate(message, 200));
        return error;
    }

    /**
     * Creates an Error object from an exception.
     *
     * @param exception the exception
     * @return Error object following the OpenAPI specification
     */
    public Error toError(Exception exception) {
        return toError(exception.getMessage());
    }

    /**
     * Truncates a message if it exceeds the maximum length.
     *
     * @param message   the original message
     * @param maxLength the maximum allowed length
     * @return the truncated message if necessary
     */
    private String truncate(String message, int maxLength) {
        if (message == null) {
            return "Error desconocido";
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 3) + "...";
    }
}
