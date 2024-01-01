package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.mapper.BookClubMapper;
import com.bahubba.bahubbabookclub.model.mapper.BookClubMembershipMapper;
import com.bahubba.bahubbabookclub.model.mapper.ReaderMapper;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.OwnershipChange;
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.service.MembershipService;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final BookClubMembershipRepo bookClubMembershipRepo;
    private final BookClubRepo bookClubRepo;
    private final BookClubMembershipMapper bookClubMembershipMapper;
    private final BookClubMapper bookClubMapper;
    private final ReaderMapper readerMapper;

    @Override
    public Page<BookClubMembershipDTO> getAll(String bookClubName, int pageNum, int pageSize)
            throws ReaderNotFoundException, UnauthorizedBookClubActionException, PageSizeTooSmallException,
                    PageSizeTooLargeException {

        // Get the reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException();
        }

        // Get the Reader's membership in the book club, ensuring they are an admin
        bookClubMembershipRepo
                .findByBookClubNameAndClubRoleAndReaderId(bookClubName, BookClubRole.ADMIN, reader.getId())
                .orElseThrow(UnauthorizedBookClubActionException::new);

        // Ensure the page size is valid
        if (pageSize < 1) {
            throw new PageSizeTooSmallException(10, getPageOfMembershipsForBookClub(bookClubName, pageNum, 10));
        } else if (pageSize > 50) {
            throw new PageSizeTooLargeException(50, 50, getPageOfMembershipsForBookClub(bookClubName, pageNum, 50));
        }

        // Get all members of the book club using the given page size
        return getPageOfMembershipsForBookClub(bookClubName, pageNum, pageSize);
    }

    @Override
    public BookClubRole getRole(String bookClubName) throws ReaderNotFoundException, MembershipNotFoundException {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException();
        }

        // Get the reader's role in the book club (if any)
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubNameAndReaderId(bookClubName, reader.getId())
                .orElseThrow(() -> new MembershipNotFoundException(reader.getUsername(), bookClubName));

        // Return the reader's role
        return membership.getClubRole();
    }

    @Override
    public BookClubMembershipDTO getMembership(String bookClubName)
            throws ReaderNotFoundException, BookClubNotFoundException {

        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException();
        }

        // Get the reader's membership in the book club (if any)
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubNameAndReaderId(bookClubName, reader.getId())
                .orElse(null);

        // If there is no membership, create a transient one with the reader and no role
        if (membership == null) {
            BookClub bookClub = bookClubRepo
                    .findByName(bookClubName)
                    .orElseThrow(() -> new BookClubNotFoundException(bookClubName));

            return BookClubMembershipDTO.builder()
                    .bookClub(bookClubMapper.entityToDTO(bookClub))
                    .reader(readerMapper.entityToDTO(reader))
                    .clubRole(BookClubRole.NONE)
                    .isOwner(false)
                    .build();
        }

        // Otherwise return the membership
        return bookClubMembershipMapper.entityToDTO(membership);
    }

    @Override
    public BookClubMembershipDTO updateMembership(MembershipUpdate membershipUpdate)
            throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException();
        }

        // Ensure the reader is not trying to update their own role
        if (reader.getId().equals(membershipUpdate.getReaderID())) {
            throw new BadBookClubActionException();
        }

        // Get the requesting reader's membership in the book club to ensure they're an admin
        bookClubMembershipRepo
                .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                        membershipUpdate.getBookClubName(), reader.getId(), BookClubRole.ADMIN)
                .orElseThrow(UnauthorizedBookClubActionException::new);

        // Get the target reader's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubNameAndReaderIdAndDepartedIsNull(
                        membershipUpdate.getBookClubName(), membershipUpdate.getReaderID())
                .orElseThrow(() -> new MembershipNotFoundException(
                        membershipUpdate.getReaderID(), membershipUpdate.getBookClubName()));

        // Ensure the target reader is not the owner of the book club, and we're not trying to change to
        // the same role
        if (membership.isOwner() || membership.getClubRole() == membershipUpdate.getRole()) {
            throw new BadBookClubActionException();
        }

        // Update the reader's role
        membership.setClubRole(membershipUpdate.getRole());
        return bookClubMembershipMapper.entityToDTO(bookClubMembershipRepo.save(membership));
    }

    @Override
    public BookClubMembershipDTO deleteMembership(String bookClubName, UUID readerID)
            throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {

        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException("Not logged in or reader not found");
        }

        // Ensure the reader is not trying to delete their own membership
        if (reader.getId().equals(readerID)) {
            throw new BadBookClubActionException();
        }

        // Get the requesting reader's membership in the book club to ensure they're an admin
        bookClubMembershipRepo
                .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                        bookClubName, reader.getId(), BookClubRole.ADMIN)
                .orElseThrow(UnauthorizedBookClubActionException::new);

        // Get the target reader's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubNameAndReaderIdAndDepartedIsNull(bookClubName, readerID)
                .orElseThrow(() -> new MembershipNotFoundException(readerID, bookClubName));

        // Ensure the target reader is not the owner of the book club
        if (membership.isOwner()) {
            throw new BadBookClubActionException();
        }

        // Delete the membership
        membership.setDeparted(LocalDateTime.now());
        return bookClubMembershipMapper.entityToDTO(bookClubMembershipRepo.save(membership));
    }

    @Override
    public Boolean changeOwnership(OwnershipChange ownershipChange)
            throws ReaderNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException("Not logged in or reader not found");
        }

        // Ensure the owner isn't trying to change ownership to themselves (the existing owner)
        if (reader.getId().equals(ownershipChange.getNewOwnerID())) {
            throw new BadBookClubActionException();
        }

        // Ensure the reader is not trying to change ownership of a book club they don't own
        bookClubMembershipRepo
                .findByBookClubNameAndReaderIdAndIsOwnerTrue(ownershipChange.getBookClubName(), reader.getId())
                .orElseThrow(UnauthorizedBookClubActionException::new);

        // Get the new owner's membership
        BookClubMembership newOwnerMembership = bookClubMembershipRepo
                .findByBookClubNameAndReaderIdAndDepartedIsNull(
                        ownershipChange.getBookClubName(), ownershipChange.getNewOwnerID())
                .orElseThrow(() -> new MembershipNotFoundException(
                        ownershipChange.getNewOwnerID(), ownershipChange.getBookClubName()));

        // Change ownership (and make the new owner an admin in case they weren't already)
        newOwnerMembership.setClubRole(BookClubRole.ADMIN);
        newOwnerMembership.setOwner(true);
        bookClubMembershipRepo.save(newOwnerMembership);

        return true;
    }

    /**
     * Get a page of memberships for a book club
     *
     * @param bookClubName The name of the book club
     * @param pageNum The page number to retrieve
     * @param pageSize The number of results per page
     * @return A page of memberships for the book club
     */
    private @NotNull Page<BookClubMembershipDTO> getPageOfMembershipsForBookClub(
            String bookClubName, int pageNum, int pageSize) {
        // Get results
        Page<BookClubMembership> entityPage = bookClubMembershipRepo.findAllByBookClubNameOrderByJoined(
                bookClubName, PageRequest.of(pageNum, pageSize));

        // Convert results to DTOs and return
        return entityPage.map(bookClubMembershipMapper::entityToDTO);
    }
}
