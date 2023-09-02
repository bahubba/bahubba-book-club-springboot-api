package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.BookClubNotFoundException;
import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.enums.RequestStatus;
import com.bahubba.bahubbabookclub.model.mapper.MembershipRequestMapper;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.repository.MembershipRequestRepo;
import com.bahubba.bahubbabookclub.service.MembershipRequestService;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link MembershipRequest} business logic implementation
 */
@Service
@Transactional
public class MembershipRequestServiceImpl implements MembershipRequestService {
    @Autowired
    private MembershipRequestRepo membershipRequestRepo;

    @Autowired
    private MembershipRequestMapper membershipRequestMapper;

    @Autowired
    private BookClubRepo bookClubRepo;

    /**
     * Create a new membership request
     * @param newMembershipRequest The new membership request's data
     * @return The new membership request's persisted entity
     */
    @Override
    public MembershipRequestDTO requestMembership(NewMembershipRequest newMembershipRequest) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException();
        }

        // Get the book club to request membership in
        BookClub bookClub = bookClubRepo
            .findByName(newMembershipRequest.getBookClubName())
            .orElseThrow(() -> new BookClubNotFoundException("Book club not found"));

        // Create the membership request and persist it
        return membershipRequestMapper
            .entityToDTO(
                membershipRequestRepo.save(
                    MembershipRequest
                        .builder()
                        .bookClub(bookClub)
                        .reader(reader)
                        .message(newMembershipRequest.getMessage())
                        .build()
                )
            );
    }

    /**
     * Check if a reader has a pending membership request for a given book club
     * @param bookClubName The name of the book club
     * @return True if the reader has a pending membership request for the book club, false otherwise
     */
    @Override
    public Boolean hasPendingRequest(String bookClubName) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException("Not logged in or reader not found");
        }

        // Check if the reader has a pending membership request for the book club
        return membershipRequestRepo.existsByBookClubNameAndReaderIdAndStatusIn(
            bookClubName,
            reader.getId(),
            List.of(RequestStatus.OPEN, RequestStatus.VIEWED)
        );
    }
}
