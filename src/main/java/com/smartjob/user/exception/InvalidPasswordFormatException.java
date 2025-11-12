package com.smartjob.user.exception;

/**
 * Exception thrown when the password format does not match the configured
 * pattern.
 */
public class InvalidPasswordFormatException extends RuntimeException {

    public InvalidPasswordFormatException(String message) {
        super(message);
    }

    public InvalidPasswordFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}