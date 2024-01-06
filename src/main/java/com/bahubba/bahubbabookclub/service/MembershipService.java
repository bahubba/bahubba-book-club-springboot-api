package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.MembershipCompositeID;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.NewOwner;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface MembershipService {

    /**
     * Get all members of a book club
     *
     * @param bookClubName The name of a book club
     * @param pageNum The page number to retrieve
     * @param pageSize The number of results per page
     * @return A list of all members of the book club
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws UnauthorizedBookClubActionException The reader was not an admin of the book club
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    Page<BookClubMembershipDTO> getAll(String bookClubName, int pageNum, int pageSize)
            throws ReaderNotFoundException, UnauthorizedBookClubActionException, PageSizeTooSmallException,
                    PageSizeTooLargeException;

    /**
     * Get the role of a reader in a book club
     *
     * @param bookClubName The name of the book club
     * @return The reader's role in the book club
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws MembershipNotFoundException The reader was not a member of the book club
     */
    BookClubRole getRole(String bookClubName) throws ReaderNotFoundException, MembershipNotFoundException;

    /**
     * Get a reader's membership in a book club
     *
     * @param bookClubName The name of the book club
     * @return The reader's membership info
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws BookClubNotFoundException The book club did not exist
     */
    BookClubMembershipDTO getMembership(String bookClubName) throws ReaderNotFoundException, BookClubNotFoundException;

    /**
     * Update a reader's role in a book club
     *
     * @param membershipUpdate The book club and reader to update
     * @return The updated membership
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws BadBookClubActionException The reader attempted to update their own role, the target
     *     reader was the owner of the book club, or there was no real change requested
     * @throws UnauthorizedBookClubActionException The reader was not an admin of the book club
     * @throws MembershipNotFoundException The target reader was not a member of the book club
     */
    BookClubMembershipDTO updateMembership(MembershipUpdate membershipUpdate)
            throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException;

    /**
     * Delete a reader's membership in a book club
     *
     * @param bookClubName The name of the book club
     * @param readerID The ID of the reader
     * @return The deleted membership
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws BadBookClubActionException The reader attempted to delete their own membership or the
     *     target reader was the owner of the book club
     * @throws UnauthorizedBookClubActionException The reader was not an admin of the book club
     * @throws MembershipNotFoundException The target reader was not a member of the book club
     */
    BookClubMembershipDTO deleteMembership(String bookClubName, UUID readerID)
            throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException;

    /**
     * Change ownership of a book club
     *
     * @param newOwner The book club and new owner ID
     * @return true if successful
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws BadBookClubActionException The reader is trying to make themselves the owner
     * @throws UnauthorizedBookClubActionException The reader was not the existing owner of the book
     *     club
     * @throws MembershipNotFoundException The target reader was not a member of the book club
     */
    Boolean addOwner(NewOwner newOwner)
            throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException;

    /**
     * Revoke a user's ownership of a book club
     *
     * @param membershipCompositeID The IDs of the book club and the user to revoke ownership from
     * @return An updated version of the membership
     * @throws ReaderNotFoundException The user was not logged in or did not exist
     * @throws BadBookClubActionException The user is trying to revoke their own ownership, or the target user was not an active owner
     * @throws UnauthorizedBookClubActionException The user was not an owner of the book
     * @throws MembershipNotFoundException The target user was not a member of the book club
     */
    BookClubMembershipDTO revokeOwnership(MembershipCompositeID membershipCompositeID)
        throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                MembershipNotFoundException;
}
