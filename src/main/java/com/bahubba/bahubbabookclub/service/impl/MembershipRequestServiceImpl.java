package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.enums.RequestAction;
import com.bahubba.bahubbabookclub.model.enums.RequestStatus;
import com.bahubba.bahubbabookclub.model.mapper.MembershipRequestMapper;
import com.bahubba.bahubbabookclub.model.payload.MembershipRequestAction;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.repository.MembershipRequestRepo;
import com.bahubba.bahubbabookclub.service.MembershipRequestService;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * {@link MembershipRequest} business logic implementation
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MembershipRequestServiceImpl implements MembershipRequestService {

    private final MembershipRequestRepo membershipRequestRepo;

    private final MembershipRequestMapper membershipRequestMapper;

    private final BookClubRepo bookClubRepo;

    private final BookClubMembershipRepo bookClubMembershipRepo;

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
            throw new ReaderNotFoundException();
        }

        // Check if the reader has a pending membership request for the book club
        return membershipRequestRepo.existsByBookClubNameAndReaderIdAndStatus(
            bookClubName,
            reader.getId(),
            RequestStatus.OPEN
        );
    }

    /**
     * Get all membership requests for a given book club
     * @param bookClubName The name of the book club
     * @param pageNum The page number to retrieve
     * @param pageSize The number of results per page
     * @return A list of membership requests for the book club
     * @throws BookClubNotFoundException If the book club doesn't exist
     * @throws ReaderNotFoundException If the reader isn't a member of the book club
     * @throws UnauthorizedBookClubActionException If the reader isn't an admin of the book club
     * @throws PageSizeTooSmallException If the page size is less than 1
     * @throws PageSizeTooLargeException If the page size is greater than 50
     */
     // TODO - custom sorting, filters?
    @Override
    public Page<MembershipRequestDTO> getMembershipRequestsForBookClub(String bookClubName, int pageNum, int pageSize) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException();
        }

        // Get the book club
        BookClub bookClub = bookClubRepo
            .findByName(bookClubName)
            .orElseThrow(() -> new BookClubNotFoundException("Book club not found"));

        // Ensure the reader is an admin of the book club
        BookClubMembership membership = bookClub.getMembers().stream()
            .filter(member -> member.getReader().getId().equals(reader.getId()))
            .findFirst()
            .orElseThrow(() -> new ReaderNotFoundException(
                reader.getUsername(), bookClub.getName()
            ));
        if(!membership.getClubRole().equals(BookClubRole.ADMIN)) {
            throw new UnauthorizedBookClubActionException();
        }

        // Ensure the page size is valid
        if(pageSize < 1) {
            // If the page size is negative, throw an error, but default the page size to 10 and return results
            throw new PageSizeTooSmallException(
                10,
                getPageOfMembershipRequestsForBookClub(bookClub.getId(), pageNum, 10)
            );
        } else if(pageSize > 50) {
            // If the page size is > 50, throw an error, but default the page size to 50 and return results
            throw new PageSizeTooLargeException(
                50,
                50,
                getPageOfMembershipRequestsForBookClub(bookClub.getId(), pageNum, 50)
            );
        }

        // Return the membership requests for the book club
        return getPageOfMembershipRequestsForBookClub(bookClub.getId(), pageNum, pageSize);
    }

    /**
     * Approve or reject a membership request
     * @param membershipRequestAction The action to take on the membership request
     * @return The updated membership request
     */
    @Override
    public MembershipRequestDTO reviewMembershipRequest(MembershipRequestAction membershipRequestAction) {
        // Get the current reader from the security context
        Reader reviewer = SecurityUtil.getCurrentUserDetails();
        if(reviewer == null) {
            throw new ReaderNotFoundException();
        }

        // Get the membership request to review
        MembershipRequest membershipRequest = membershipRequestRepo
            .findById(membershipRequestAction.getMembershipRequest().getId())
            .orElseThrow(() -> new MembershipRequestNotFoundException(
                membershipRequestAction.getMembershipRequest().getReader().getUsername(),
                membershipRequestAction.getMembershipRequest().getBookClub().getName()
            ));

        // Ensure the reviewer is an admin of the book club
        BookClubMembership reviewerMembership = membershipRequest.getBookClub().getMembers().stream()
            .filter(member -> member.getReader().getId().equals(reviewer.getId()))
            .findFirst()
            .orElseThrow(() -> new ReaderNotFoundException(
                reviewer.getUsername(), membershipRequest.getBookClub().getName()
            ));
        if(!reviewerMembership.getClubRole().equals(BookClubRole.ADMIN)) {
            throw new UnauthorizedBookClubActionException();
        }

        // Ensure the membership request is still open
        if(!membershipRequest.getStatus().equals(RequestStatus.OPEN)) {
            throw new BadBookClubActionException();
        }

        // Attempt to add the reader to the book club with the specified role if the request was approved
        if(membershipRequestAction.getAction().equals(RequestAction.APPROVE)) {
            // Ensure the reader isn't already a member of the book club
            BookClubMembership existingMembership = membershipRequest.getBookClub().getMembers().stream()
                .filter(member -> member.getReader().getId().equals(membershipRequest.getReader().getId()))
                .findFirst()
                .orElse(null);

            if(existingMembership != null) {
                throw new BadBookClubActionException();
            }

            // Add the reader to the book club
            bookClubMembershipRepo.save(BookClubMembership
                .builder()
                .bookClub(membershipRequest.getBookClub())
                .reader(membershipRequest.getReader())
                .clubRole(membershipRequestAction.getRole())
                .build()
            );
        }

        // Update the membership request
        membershipRequest.setStatus(
            membershipRequestAction.getAction().equals(RequestAction.APPROVE)
                ? RequestStatus.APPROVED
                : RequestStatus.REJECTED
        );
        membershipRequest.setRole(
            membershipRequestAction.getAction().equals(RequestAction.APPROVE)
                ? membershipRequestAction.getRole()
                : BookClubRole.NONE
        );
        membershipRequest.setReviewer(reviewer);
        membershipRequest.setReviewMessage(membershipRequestAction.getReviewMessage());
        membershipRequest.setReviewed(LocalDateTime.now());

        // Persist the updated membership request and return it
        return membershipRequestMapper.entityToDTO(membershipRequestRepo.save(membershipRequest));
    }

    /**
     * Get a page of results for all membership requests for a given book club
     * @param bookClubID The ID of the book club
     * @param pageNum The page number to retrieve
     * @param pageSize The number of results per page
     */
    private Page<MembershipRequestDTO> getPageOfMembershipRequestsForBookClub(UUID bookClubID, int pageNum, int pageSize) {
        // Get results
        Page<MembershipRequest> entityPage = membershipRequestRepo.findAllByBookClubIdOrderByRequestedDesc(
            bookClubID, PageRequest.of(pageNum, pageSize)
        );

        // Convert results to DTOs and return
        return entityPage.map(membershipRequestMapper::entityToDTO);
    }
}
