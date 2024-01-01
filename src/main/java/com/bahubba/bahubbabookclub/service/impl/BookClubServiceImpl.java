package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.Notification;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.enums.NotificationType;
import com.bahubba.bahubbabookclub.model.enums.Publicity;
import com.bahubba.bahubbabookclub.model.mapper.BookClubMapper;
import com.bahubba.bahubbabookclub.model.mapper.BookClubMembershipMapper;
import com.bahubba.bahubbabookclub.model.mapper.ReaderMapper;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.repository.NotificationRepo;
import com.bahubba.bahubbabookclub.service.BookClubService;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link BookClub} business logic implementation */
@Service
@Transactional
@RequiredArgsConstructor
public class BookClubServiceImpl implements BookClubService {

    private final BookClubRepo bookClubRepo;
    private final BookClubMapper bookClubMapper;
    private final ReaderMapper readerMapper;
    private final BookClubMembershipMapper bookClubMembershipMapper;
    private final BookClubMembershipRepo bookClubMembershipRepo;
    private final NotificationRepo notificationRepo;

    @Override
    public BookClubDTO create(NewBookClub newBookClub) throws ReaderNotFoundException {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException();
        }

        // Convert the book club to an entity, add the reader as a member/owner, and persist it
        BookClub newBookClubEntity = bookClubMapper.modelToEntity(newBookClub);
        newBookClubEntity = bookClubRepo.save(newBookClubEntity);

        // Add the reader as a member/owner
        bookClubMembershipRepo.save(BookClubMembership.builder()
                .bookClub(newBookClubEntity)
                .reader(reader)
                .clubRole(BookClubRole.ADMIN)
                .isOwner(true)
                .build());

        // Generate a notification for the book club's creation
        notificationRepo.save(Notification.builder()
                .sourceReader(reader)
                .targetReader(reader)
                .bookClub(newBookClubEntity)
                .type(NotificationType.BOOK_CLUB_CREATED)
                .build());

        return bookClubMapper.entityToDTO(newBookClubEntity);
    }

    public BookClubDTO update(BookClubDTO bookClubDTO) throws ReaderNotFoundException, BookClubNotFoundException {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException();
        }

        // Find the book club to update
        BookClub bookClub = bookClubRepo
                .findById(bookClubDTO.getId())
                .orElseThrow(() -> new BookClubNotFoundException(bookClubDTO.getId()));

        // Update the book club's metadata
        bookClub.setName(bookClubDTO.getName());
        bookClub.setDescription(bookClubDTO.getDescription());
        bookClub.setImageURL(bookClubDTO.getImageURL());
        bookClub.setPublicity(bookClubDTO.getPublicity());

        // Persist the updated book club
        bookClub = bookClubRepo.save(bookClub);

        // TODO - Add notifications for each piece of metadata that was updated

        return bookClubMapper.entityToDTO(bookClub);
    }

    @Override
    public BookClubDTO findByID(UUID id)
            throws BookClubNotFoundException, ReaderNotFoundException, MembershipNotFoundException {

        // Get the book club by ID
        BookClub bookClub = bookClubRepo.findById(id).orElseThrow(() -> new BookClubNotFoundException(id));

        // If the book club is not private, return it
        if (bookClub.getPublicity() != Publicity.PRIVATE) {
            return bookClubMapper.entityToDTO(bookClub);
        }

        // Otherwise, check if the current reader is a member of the book club
        return checkBookClubMembership(bookClub);
    }

    @Override
    public BookClubDTO findByName(String name)
            throws BookClubNotFoundException, ReaderNotFoundException, MembershipNotFoundException {
        // Get the book club by name
        BookClub bookClub = bookClubRepo.findByName(name).orElseThrow(() -> new BookClubNotFoundException(name));

        // If the book club is not private, return it
        if (bookClub.getPublicity() != Publicity.PRIVATE) {
            return bookClubMapper.entityToDTO(bookClub);
        }

        // Otherwise, check if the current reader is a member of the book club
        return checkBookClubMembership(bookClub);
    }

    @Override
    public Page<BookClubDTO> findAllForReader(int pageNum, int pageSize)
            throws ReaderNotFoundException, PageSizeTooSmallException, PageSizeTooLargeException {

        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException();
        }

        // Ensure the page size is appropriate
        if (pageSize < 0) {
            throw new PageSizeTooSmallException(10, getPageOfAllForReader(reader.getId(), pageNum, 10));
        } else if (pageSize > 50) {
            throw new PageSizeTooLargeException(50, 50, getPageOfAllForReader(reader.getId(), pageNum, 50));
        }

        return getPageOfAllForReader(reader.getId(), pageNum, pageSize);
    }

    @Override
    public Page<BookClubDTO> findAll(int pageNum, int pageSize)
            throws PageSizeTooSmallException, PageSizeTooLargeException {

        // Ensure the page size is appropriate
        if (pageSize < 0) {
            // If the page size is negative, throw an error, but default the page size to 10 and return
            // results
            throw new PageSizeTooSmallException(10, getPageOfAll(pageNum, 10));
        } else if (pageSize > 50) {
            // If the page size is > 50, throw an error, but default the page size to 50 and return
            // results
            throw new PageSizeTooLargeException(50, 50, getPageOfAll(pageNum, 50));
        }

        // Get results using the appropriate page size
        return getPageOfAll(pageNum, pageSize);
    }

    @Override
    public Page<BookClubDTO> search(String searchTerm, int pageNum, int pageSize)
            throws PageSizeTooSmallException, PageSizeTooLargeException {

        // Ensure the page size is appropriate
        if (pageSize < 1) {
            // If the page size is negative, throw an error, but default the page size to 10 and return
            // results
            throw new PageSizeTooSmallException(10, getPageOfSearchResults(searchTerm, pageNum, 10));
        } else if (pageSize > 50) {
            // If the page size is > 50, throw an error, but default the page size to 50 and return
            // results
            throw new PageSizeTooLargeException(50, 50, getPageOfSearchResults(searchTerm, pageNum, 50));
        }

        // Get results using the appropriate page size
        return getPageOfSearchResults(searchTerm, pageNum, pageSize);
    }

    @Override
    public BookClubDTO disbandBookClubByID(UUID id)
            throws ReaderNotFoundException, MembershipNotFoundException, UnauthorizedBookClubActionException,
                    BadBookClubActionException {

        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException();
        }

        // Find the reader's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubIdAndReaderId(id, reader.getId())
                .orElseThrow(() -> new MembershipNotFoundException(reader.getId(), id));

        return disbandBookClub(membership);
    }

    @Override
    public BookClubDTO disbandBookClubByName(String name)
            throws ReaderNotFoundException, MembershipNotFoundException, UnauthorizedBookClubActionException,
                    BadBookClubActionException {

        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException();
        }

        // Find the reader's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubNameAndReaderId(name, reader.getId())
                .orElseThrow(() -> new MembershipNotFoundException(reader.getUsername(), name));

        return disbandBookClub(membership);
    }

    /**
     * Ensure a reader is a member of a book club before returning the book club
     *
     * @param bookClub The book club to check
     * @return The book club if the reader is a member
     * @throws ReaderNotFoundException The reader was not logged in
     * @throws MembershipNotFoundException The reader was not a member of the book club
     */
    private BookClubDTO checkBookClubMembership(BookClub bookClub)
            throws ReaderNotFoundException, MembershipNotFoundException {

        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if (reader == null) {
            throw new ReaderNotFoundException();
        }

        // Check if the reader is a member of the book club
        if (!bookClubMembershipRepo.existsByBookClubIdAndReaderId(bookClub.getId(), reader.getId())) {
            throw new MembershipNotFoundException(reader.getUsername(), bookClub.getName());
        }

        return bookClubMapper.entityToDTO(bookClub);
    }

    /**
     * Retrieves a page of book clubs that the reader has some role in
     *
     * @param readerID The UUID of the reader
     * @param pageNum The page number
     * @param pageSize The number of results per page
     */
    private @NotNull Page<BookClubDTO> getPageOfAllForReader(UUID readerID, int pageNum, int pageSize) {
        // Get results
        Page<BookClub> entityPage = bookClubRepo.findAllForReader(readerID, PageRequest.of(pageNum, pageSize));

        // Convert results to DTOs and return
        return entityPage.map(bookClubMapper::entityToDTO);
    }

    /**
     * Retrieves a page of all book clubs
     *
     * @param pageNum The page number
     * @param pageSize The number of results per page
     */
    private @NotNull Page<BookClubDTO> getPageOfAll(int pageNum, int pageSize) {
        // Get results
        Page<BookClub> entityPage = bookClubRepo.findAll(PageRequest.of(pageNum, pageSize));

        // Convert results to DTOs and return
        return entityPage.map(bookClubMapper::entityToDTO);
    }

    /**
     * Searches for book clubs by name, returning a paged subset
     *
     * @param searchTerm the substring of the name to search for
     * @param pageNum the page number
     * @param pageSize the size of the page
     */
    private @NotNull Page<BookClubDTO> getPageOfSearchResults(String searchTerm, int pageNum, int pageSize) {
        // Get results
        Page<BookClub> entityPage = bookClubRepo.findAllByPublicityNotAndNameContainsIgnoreCase(
                Publicity.PRIVATE, searchTerm, PageRequest.of(pageNum, pageSize));

        // Convert results to DTOs and return
        return entityPage.map(bookClubMapper::entityToDTO);
    }

    /**
     * Disband a book club
     *
     * @param membership The membership of the reader in the book club to disband
     * @return The disbanded book club
     * @throws UnauthorizedBookClubActionException The reader was not the owner of the book club
     * @throws BadBookClubActionException The book club was already disbanded
     */
    private BookClubDTO disbandBookClub(@NotNull BookClubMembership membership)
            throws UnauthorizedBookClubActionException, BadBookClubActionException {
        // Ensure the reader is the owner of the book club
        if (!membership.isOwner()) {
            throw new UnauthorizedBookClubActionException();
        }

        // Ensure the book club is not already disbanded
        BookClub bookClub = membership.getBookClub();
        if (bookClub.getDisbanded() != null) {
            throw new BadBookClubActionException();
        }

        // Disband the book club
        bookClub.setDisbanded(LocalDateTime.now());
        return bookClubMapper.entityToDTO(bookClubRepo.save(bookClub));
    }
}
