package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.payload.MembershipRequestAction;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;
import com.bahubba.bahubbabookclub.model.payload.PaginatedPayload;
import com.bahubba.bahubbabookclub.service.MembershipRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Membership Request endpoints
 */
@RestController
@RequestMapping("/api/v1/membership-requests")
@RequiredArgsConstructor
public class MembershipRequestController {
    private final MembershipRequestService membershipRequestService;

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
     * @return true if the user has a pending request, false otherwise
     */
    @GetMapping("/has-pending-request/{bookClubName}")
    public ResponseEntity<Boolean> hasPendingRequest(@PathVariable String bookClubName) {
        return ResponseEntity.ok(membershipRequestService.hasPendingRequest(bookClubName));
    }

    /**
     * Get all memberships for a given book club
     * @param bookClubName name of the book club
     * @return list of membership requests for the book club
     */
    @GetMapping("/all-for-club/{bookClubName}")
    public ResponseEntity<Page<MembershipRequestDTO>> getMembershipRequestsForBookClub(
        @PathVariable String bookClubName,
        @RequestParam int pageNum,
        @RequestParam int pageSize
    ) {
        return ResponseEntity
            .ok(membershipRequestService.getMembershipRequestsForBookClub(bookClubName, pageNum, pageSize));
    }

    /**
     * Approve or reject a membership request
     * @param membershipRequestAction approval or rejection of the request
     * @return updated version of the membership request
     */
    @PatchMapping("/review")
    public ResponseEntity<MembershipRequestDTO> reviewMembershipRequest(
        @RequestBody MembershipRequestAction membershipRequestAction
    ) {
        return ResponseEntity.ok(membershipRequestService.reviewMembershipRequest(membershipRequestAction));
    }
}
