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
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.service.MembershipService;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MembershipServiceImpl implements MembershipService {
    @Autowired
    private BookClubMembershipRepo bookClubMembershipRepo;

    @Autowired
    private BookClubRepo bookClubRepo;

    @Autowired
    private BookClubMembershipMapper bookClubMembershipMapper;

    @Autowired
    private BookClubMapper bookClubMapper;

    @Autowired
    private ReaderMapper readerMapper;

    /**
     * Get all members of a book club
     * @param bookClubName The name of a book club
     * @return A list of all members of the book club
     */
    @Override
    public List<BookClubMembershipDTO> getAll(String bookClubName) {
        // Get the reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException();
        }

        // Get the Reader's membership in the book club, ensuring they are an admin
        bookClubMembershipRepo
            .findByBookClubNameAndClubRoleAndReaderId(bookClubName, BookClubRole.ADMIN, reader.getId())
            .orElseThrow(UnauthorizedBookClubActionException::new);

        // Get all members of the book club
        return bookClubMembershipMapper
            .entityListToDTOList(bookClubMembershipRepo.findAllByBookClubNameOrderByJoined(bookClubName));
    }

    /**
     * Get the role of a reader in a book club
     * @param bookClubName The name of the book club
     * @return The reader's role in the book club
     */
    @Override
    public BookClubRole getRole(String bookClubName) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException();
        }

        // Get the reader's role in the book club (if any)
        BookClubMembership membership = bookClubMembershipRepo
            .findByBookClubNameAndReaderId(bookClubName, reader.getId())
            .orElseThrow(() -> new MembershipNotFoundException(reader.getUsername(), bookClubName));

        // Return the reader's role
        return membership.getClubRole();
    }

    /**
     * Get a reader's membership in a book club (or lack thereof) and the book club
     * @param bookClubName The name of the book club
     * @return The reader's membership in the book club (or lack thereof) and the book club
     */
    public BookClubMembershipDTO getMembership(String bookClubName) throws ReaderNotFoundException {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException();
        }

        // Get the reader's membership in the book club (if any)
        BookClubMembership membership = bookClubMembershipRepo.findByBookClubNameAndReaderId(bookClubName, reader.getId()).orElse(null);

        // If there is no membership, create a transient one with the reader and no role
        if(membership == null) {
            BookClub bookClub = bookClubRepo.findByName(bookClubName).orElseThrow(() -> new BookClubNotFoundException(bookClubName));

            return BookClubMembershipDTO
                .builder()
                .bookClub(bookClubMapper.entityToDTO(bookClub))
                .reader(readerMapper.entityToDTO(reader))
                .clubRole(BookClubRole.NONE)
                .isCreator(false)
                .build();
        }

        // Otherwise return the membership
        return bookClubMembershipMapper.entityToDTO(membership);
    }

    /**
     * Update a reader's role in a book club
     * @param membershipUpdate The book club and reader to update
     * @return The updated membership
     */
    @Override
    public BookClubMembershipDTO updateMembership(MembershipUpdate membershipUpdate) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException("Not logged in or reader not found");
        }

        // Ensure the reader is not trying to update their own role
        if(reader.getId().equals(membershipUpdate.getReaderID())) {
            throw new BadBookClubActionException();
        }

        // Get the requesting reader's membership in the book club to ensure they're an admin
        bookClubMembershipRepo
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(membershipUpdate.getBookClubName(), reader.getId(), BookClubRole.ADMIN)
            .orElseThrow(UnauthorizedBookClubActionException::new);

        // Get the target reader's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
            .findByBookClubNameAndReaderIdAndDepartedIsNull(membershipUpdate.getBookClubName(), membershipUpdate.getReaderID())
            .orElseThrow(() -> new MembershipNotFoundException(membershipUpdate.getReaderID(), membershipUpdate.getBookClubName()));

        // Ensure the target reader is not the creator of the book club, and we're not trying to change to the same role
        if(membership.isCreator() || membership.getClubRole() == membershipUpdate.getRole()) {
            throw new BadBookClubActionException();
        }

        // Update the reader's role
        membership.setClubRole(membershipUpdate.getRole());
        return bookClubMembershipMapper.entityToDTO(bookClubMembershipRepo.save(membership));
    }

    /**
     * Delete a reader's membership in a book club
     * @param bookClubName The name of the book club
     * @param readerID The ID of the reader
     * @return The deleted membership
     */
    @Override
    public BookClubMembershipDTO deleteMembership(String bookClubName, UUID readerID) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException("Not logged in or reader not found");
        }

        // Ensure the reader is not trying to delete their own membership
        if(reader.getId().equals(readerID)) {
            throw new BadBookClubActionException();
        }

        // Get the requesting reader's membership in the book club to ensure they're an admin
        bookClubMembershipRepo
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(bookClubName, reader.getId(), BookClubRole.ADMIN)
            .orElseThrow(UnauthorizedBookClubActionException::new);

        // Get the target reader's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
            .findByBookClubNameAndReaderIdAndDepartedIsNull(bookClubName, readerID)
            .orElseThrow(() -> new MembershipNotFoundException(readerID, bookClubName));

        // Ensure the target reader is not the creator of the book club
        if(membership.isCreator()) {
            throw new BadBookClubActionException();
        }

        // Delete the membership
        membership.setDeparted(LocalDateTime.now());
        return bookClubMembershipMapper.entityToDTO(bookClubMembershipRepo.save(membership));
    }
}
