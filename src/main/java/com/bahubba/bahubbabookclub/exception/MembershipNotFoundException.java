package com.bahubba.bahubbabookclub.exception;

import java.util.UUID;

/**
 * Custom exception for when a client searches for a book club that doesn't exist (in an active
 * state)
 */
public class MembershipNotFoundException extends RuntimeException {

    /**
     * Generates exception for missing book club membership by reader ID and book club ID
     *
     * @param readerId reader ID
     * @param bookClubId book club ID
     */
    public MembershipNotFoundException(UUID readerId, UUID bookClubId) {
        super("Reader with ID '" + readerId + "' does not have a membership in book club with ID '" + bookClubId + "'");
    }

    /**
     * Generates exception for missing book club membership by reader username and book club name
     *
     * @param username reader username
     * @param bookClubName book club name
     */
    public MembershipNotFoundException(String username, String bookClubName) {
        super("Reader '" + username + "' does not have a membership in book club '" + bookClubName + "'");
    }

    /**
     * Generates exception for missing book club membership by reader ID and book club name
     *
     * @param readerID reader ID
     * @param bookClubName book club name
     */
    public MembershipNotFoundException(UUID readerID, String bookClubName) {
        super("Reader with ID '" + readerID + "' does not have a membership in '" + bookClubName + "'");
    }
}
