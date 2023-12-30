package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.OwnershipChange;
import com.bahubba.bahubbabookclub.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {
    private final MembershipService membershipService;

    /**
     * Get all users in a book club
     * @param bookClubName name of the book club
     * @return list of users in the book club
     */
    @GetMapping("/all/{bookClubName}")
    public ResponseEntity<List<BookClubMembershipDTO>> getAll(@PathVariable String bookClubName) {
        return ResponseEntity.ok(membershipService.getAll(bookClubName));
    }

    /**
     * Get a non-private book club and the user's role (or lack thereof) in it
     * @param bookClubName name of the book club
     * @return book club and user's role in it
     */
    @GetMapping("/{bookClubName}")
    public ResponseEntity<BookClubMembershipDTO> getMembership(@PathVariable String bookClubName) {
        return ResponseEntity.ok(membershipService.getMembership(bookClubName));
    }

    /**
     * Gets user's role in a book club
     * @param bookClubName name of the book club
     * @return user's role in the book club
     */
    @GetMapping("/role/{bookClubName}")
    public ResponseEntity<BookClubRole> getRole(@PathVariable String bookClubName) {
        return ResponseEntity.ok(membershipService.getRole(bookClubName));
    }

    /**
     * Update a reader's role in a book club
     * @param membershipUpdate book club name, reader ID, and new role
     * @return reader's new membership
     */
    @PatchMapping
    public ResponseEntity<BookClubMembershipDTO> updateMembership(@RequestBody MembershipUpdate membershipUpdate) {
        return ResponseEntity.ok(membershipService.updateMembership(membershipUpdate));
    }

    /**
     * Delete a reader's membership in a book club
     * @param bookClubName name of the book club
     * @return reader's new membership
     */
    @DeleteMapping("{bookClubName}/{readerID}")
    public ResponseEntity<BookClubMembershipDTO> deleteMembership(
        @PathVariable String bookClubName,
        @PathVariable UUID readerID
    ) {
        return ResponseEntity.ok(membershipService.deleteMembership(bookClubName, readerID));
    }

    /**
     * Change ownership of a book club
     * TODO - Change "creator" terminology to "owner"
     * @param ownershipChange book club name and new owner ID
     * @return true if successful
     */
    @PatchMapping("/change-ownership")
    public ResponseEntity<Boolean> changeOwnership(@RequestBody OwnershipChange ownershipChange) {
        membershipService.changeOwnership(ownershipChange);
        return ResponseEntity.ok(true);
    }
}
