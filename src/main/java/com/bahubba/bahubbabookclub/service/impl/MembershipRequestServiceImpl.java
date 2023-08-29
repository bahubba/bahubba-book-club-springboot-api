package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.BookClubNotFoundException;
import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.mapper.MembershipRequestMapper;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.repository.MembershipRequestRepo;
import com.bahubba.bahubbabookclub.service.MembershipRequestService;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
            throw new ReaderNotFoundException("Not logged in or reader not found");
        }

        // Get the book club to request membership in
        BookClub bookClub = bookClubRepo
            .findById(newMembershipRequest.getBookClubId())
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
}
