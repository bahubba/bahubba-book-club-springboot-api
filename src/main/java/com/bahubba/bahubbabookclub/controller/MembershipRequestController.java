package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;
import com.bahubba.bahubbabookclub.service.MembershipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Membership Request endpoints
 */
@RestController
@RequestMapping("/api/v1/membership-requests")
public class MembershipRequestController {
    @Autowired
    private MembershipRequestService membershipRequestService;

    /**
     * Creates a membership request
     * @param newMembershipRequest ID of the book club and message for the request
     * @return persisted version of the new membership request
     */
    @PostMapping("/request-membership")
    public ResponseEntity<MembershipRequestDTO> requestMembership(@RequestBody NewMembershipRequest newMembershipRequest) {
        return ResponseEntity.ok(membershipRequestService.requestMembership(newMembershipRequest));
    }

    /**
     * See if a user has a pending request for a given book club
     * @param bookClubName name of the book club
     */
    @PostMapping("/has-pending-request")
    public ResponseEntity<Boolean> hasPendingRequest(@RequestBody String bookClubName) {
        return ResponseEntity.ok(membershipRequestService.hasPendingRequest(bookClubName));
    }
}
