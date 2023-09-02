package com.bahubba.bahubbabookclub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Custom exception for when a membership request that doesn't exist
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Membership request not found")
public class MembershipRequestNotFoundException extends RuntimeException {
    /**
     * Generates exception for missing membership request by Reader and Book Club IDs
     * @param username the username of the reader requesting membership
     * @param bookClubName the name of the book club the reader is requesting membership in
     */
    public MembershipRequestNotFoundException(String username, String bookClubName) {
        super("Could not find request for membership in '" + bookClubName + "' by '" + username + "'");
    }
}
