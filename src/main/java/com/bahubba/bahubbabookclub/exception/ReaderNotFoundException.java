package com.bahubba.bahubbabookclub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Custom exception for when a client searches for a reader (user) that doesn't exist (in an active state)
 */
public class ReaderNotFoundException extends RuntimeException {

    /**
     * Generates exception for missing reader in security context
     */
    public ReaderNotFoundException() {
        super("Not logged in or reader not found");
    }

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

    /**
     * Generates an exception for a reader not being found in a book club
     */
    public ReaderNotFoundException(String username, String bookClubName) {
        super("Reader '" + username + "' not found in book club '" + bookClubName + "'");
    }
}
