package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import com.bahubba.bahubbabookclub.model.payload.MembershipRequestAction;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;
import org.springframework.data.domain.Page;

/** {@link MembershipRequest} service layer */
public interface MembershipRequestService {

    /**
     * Request membership for a reader in a book club
     *
     * @param newMembershipRequest The new membership request's data
     * @return The new membership request's persisted entity
     * @throws ReaderNotFoundException The reader was not found
     * @throws BookClubNotFoundException The book club was not found
     */
    MembershipRequestDTO requestMembership(NewMembershipRequest newMembershipRequest)
            throws ReaderNotFoundException, BookClubNotFoundException;

    /**
     * Check if a reader has a pending membership request for a given book club
     *
     * @param bookClubName The name of the book club
     * @return True if the reader has a pending membership request for the book club, false otherwise
     * @throws ReaderNotFoundException The reader was not found
     */
    Boolean hasPendingRequest(String bookClubName) throws ReaderNotFoundException;

    /**
     * Get all membership requests for a given book club
     *
     * @param bookClubName The name of the book club
     * @param pageNum The page number to retrieve
     * @param pageSize The number of results per page
     * @return A list of membership requests for the book club
     * @throws ReaderNotFoundException The reader was not found or was not in the book club
     * @throws BookClubNotFoundException The book club did not exist
     * @throws UnauthorizedBookClubActionException The reader was not an admin of the book club
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    Page<MembershipRequestDTO> getMembershipRequestsForBookClub(String bookClubName, int pageNum, int pageSize)
            throws ReaderNotFoundException, BookClubNotFoundException, UnauthorizedBookClubActionException,
                    PageSizeTooSmallException, PageSizeTooLargeException;

    /**
     * Approve or reject a membership request
     *
     * @param membershipRequestAction The action to take on the membership request
     * @return The updated membership request
     * @throws ReaderNotFoundException The reader was not found or was not in the book club
     * @throws MembershipRequestNotFoundException The membership request was not found
     * @throws UnauthorizedBookClubActionException The reader was not an admin of the book club
     * @throws BadBookClubActionException The membership request was not open or the target reader was
     *     already a member
     */
    MembershipRequestDTO reviewMembershipRequest(MembershipRequestAction membershipRequestAction)
            throws ReaderNotFoundException, MembershipRequestNotFoundException, UnauthorizedBookClubActionException,
                    BadBookClubActionException;
}
