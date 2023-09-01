package com.bahubba.bahubbabookclub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Custom exception for when a client searches for a book club that doesn't exist (in an active state)
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Book Club Membership not found")
public class MembershipNotFoundException extends RuntimeException {

    /**
     * Generates exception for missing book club membership by reader ID and Book Club name
     * @param username reader username
     * @param bookClubName book club name
     */
    public MembershipNotFoundException(String username, String bookClubName) {
        super("Reader '" + username + "' does not have a membership in book club '" + bookClubName + "'");
    }
}
