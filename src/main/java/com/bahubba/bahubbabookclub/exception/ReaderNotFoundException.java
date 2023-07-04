package com.bahubba.bahubbabookclub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Custom exception for when a client searches for a reader (user) that doesn't exist (in an active state)
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Reader not found")
public class ReaderNotFoundException extends RuntimeException {

    /**
     * Generates exception for missing reader by username or email
     * @param usernameOrEmail reader username or email
     */
    public ReaderNotFoundException(String usernameOrEmail) {
        super("Reader could not be found with username or email matching '" + usernameOrEmail + "'");
    }

    /**
     * Generates exception for missing reader by username or email
     * @param id reader ID
     */
    public ReaderNotFoundException(UUID id) {
        super("Reader could not be found with ID '" + id + "'");
    }
}
