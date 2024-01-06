package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.MembershipCompositeID;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.NewOwner;
import com.bahubba.bahubbabookclub.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/memberships")
@Tag(name = "Membership Controller", description = "Book Club Membership endpoints")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    /**
     * Get all users in a book club
     *
     * @param bookClubName The name of the book club
     * @return A page of users in the book club
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws UnauthorizedBookClubActionException The reader was not an admin of the book club
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    @GetMapping("/all/{bookClubName}")
    @Operation(summary = "Get All Members", description = "Gets all members of a book club")
    public ResponseEntity<Page<BookClubMembershipDTO>> getAll(
            @PathVariable String bookClubName, @RequestParam int pageNum, @RequestParam int pageSize)
            throws ReaderNotFoundException, UnauthorizedBookClubActionException, PageSizeTooSmallException,
                    PageSizeTooLargeException {

        return ResponseEntity.ok(membershipService.getAll(bookClubName, pageNum, pageSize));
    }

    /**
     * Get a non-private book club and the user's role in it
     *
     * @param bookClubName The name of the book club
     * @return The reader's membership in the book club
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws BookClubNotFoundException The book club did not exist
     */
    @GetMapping("/{bookClubName}")
    @Operation(summary = "Get Membership", description = "Gets a reader's membership in a book club")
    public ResponseEntity<BookClubMembershipDTO> getMembership(@PathVariable String bookClubName)
            throws ReaderNotFoundException, BookClubNotFoundException {

        return ResponseEntity.ok(membershipService.getMembership(bookClubName));
    }

    /**
     * Get the user's role in a book club
     *
     * @param bookClubName The name of the book club
     * @return The user's role in the book club
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws MembershipNotFoundException The reader was not a member of the book club
     */
    @GetMapping("/role/{bookClubName}")
    @Operation(summary = "Get Role", description = "Gets the reader's role in a book club")
    public ResponseEntity<BookClubRole> getRole(@PathVariable String bookClubName)
            throws ReaderNotFoundException, MembershipNotFoundException {

        return ResponseEntity.ok(membershipService.getRole(bookClubName));
    }

    /**
     * Update a reader's role in a book club
     *
     * @param membershipUpdate The book club's name, reader's ID, and new role
     * @return The reader's new membership
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws BadBookClubActionException The reader attempted to update their own role, the target
     *     reader was the owner of the book club, or there was no real change requested
     * @throws UnauthorizedBookClubActionException The reader was not an admin of the book club
     * @throws MembershipNotFoundException The target reader was not a member of the book club
     */
    @PatchMapping
    @Operation(summary = "Update Membership", description = "Updates a reader's membership in a book club")
    public ResponseEntity<BookClubMembershipDTO> updateMembership(@RequestBody MembershipUpdate membershipUpdate)
            throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {

        return ResponseEntity.ok(membershipService.updateMembership(membershipUpdate));
    }

    /**
     * Delete a reader's membership in a book club
     *
     * @param bookClubName The name of the book club
     * @return The reader's new membership
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws BadBookClubActionException The reader attempted to delete their own membership or the
     *     target reader was the owner of the book club
     * @throws UnauthorizedBookClubActionException The reader was not an admin of the book club
     * @throws MembershipNotFoundException The target reader was not a member of the book club
     */
    @DeleteMapping("{bookClubName}/{readerID}")
    @Operation(summary = "Delete Membership", description = "Deletes a reader's membership in a book club")
    public ResponseEntity<BookClubMembershipDTO> deleteMembership(
            @PathVariable String bookClubName, @PathVariable UUID readerID)
            throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {

        return ResponseEntity.ok(membershipService.deleteMembership(bookClubName, readerID));
    }

    /**
     * Change ownership of a book club
     *
     * @param newOwner The book club's name and new owner's ID
     * @return A message with whether the change was successful
     * @throws ReaderNotFoundException The reader was not logged in or did not exist
     * @throws BadBookClubActionException The reader is trying to make themselves the owner
     * @throws UnauthorizedBookClubActionException The reader was not the existing owner of the book
     *     club
     * @throws MembershipNotFoundException The target reader was not a member of the book club
     */
    @PatchMapping("/add-owner")
    @Operation(summary = "Add Owner", description = "Adds a new owner to a book club")
    public ResponseEntity<Boolean> addOwner(@RequestBody NewOwner newOwner)
            throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {

        membershipService.addOwner(newOwner);
        return ResponseEntity.ok(true);
    }

    /**
     * Revoke a user's ownership of a book club
     *
     * @param membershipCompositeID The book club's and user's IDs
     * @return The updated membership
     * @throws ReaderNotFoundException The user was not logged in or did not exist
     * @throws BadBookClubActionException The user is trying to revoke their own ownership, or the target user was not an active owner
     * @throws UnauthorizedBookClubActionException The user was not an owner of the book
     * @throws MembershipNotFoundException The target user was not a member of the book club
     */
    @PatchMapping("/revoke-ownership")
    @Operation(summary = "Revoke Ownership", description = "Revoke ownership of a book club from a user")
    public ResponseEntity<BookClubMembershipDTO> revokeOwnership(
            @RequestBody MembershipCompositeID membershipCompositeID)
            throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {

        return ResponseEntity.ok(membershipService.revokeOwnership(membershipCompositeID));
    }
}
