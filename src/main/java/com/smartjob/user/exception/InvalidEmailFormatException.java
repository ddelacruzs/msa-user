package com.smartjob.user.exception;

/**
 * Exception thrown when the email format does not match the configured pattern.
 */
public class InvalidEmailFormatException extends RuntimeException {
    public InvalidEmailFormatException(String message) {
        super(message);
    }

    public InvalidEmailFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
