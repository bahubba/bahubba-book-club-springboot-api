package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * {@link BookClub} business logic implementation
 */
@Service
@Transactional
public class BookClubServiceImpl implements BookClubService {
    @Autowired
    private BookClubRepo bookClubRepo;

    @Autowired
    private BookClubMapper bookClubMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private BookClubMembershipMapper bookClubMembershipMapper;

    @Autowired
    private BookClubMembershipRepo bookClubMembershipRepo;

    @Autowired
    private NotificationRepo notificationRepo;

    /**
     * Create a new book club
     * @param newBookClub Metadata for the new book club
     * @return The new book club's persisted entity
     */
    @Override
    public BookClubDTO create(NewBookClub newBookClub) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException();
        }

        // Convert the book club to an entity, add the reader as a member/creator, and persist it
        BookClub newBookClubEntity = bookClubMapper.modelToEntity(newBookClub);
        newBookClubEntity = bookClubRepo.save(newBookClubEntity);

        // Add the reader as a member/creator
        bookClubMembershipRepo.save(BookClubMembership
            .builder()
            .bookClub(newBookClubEntity)
            .reader(reader)
            .clubRole(BookClubRole.ADMIN)
            .isCreator(true)
            .build()
        );

        // Generate a notification for the book club's creation
        notificationRepo.save(
            Notification
                .builder()
                .sourceReader(reader)
                .targetReader(reader)
                .bookClub(newBookClubEntity)
                .type(NotificationType.BOOK_CLUB_CREATED)
                .build()
        );

        return bookClubMapper.entityToDTO(newBookClubEntity);
    }

    /**
     * Update a book club
     * @param bookClubDTO The book club's new metadata
     * @return The updated book club's persisted entity
     */
    public BookClubDTO update(BookClubDTO bookClubDTO) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
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

    /**
     * Find a book club by its ID
     * @param id The ID of the book club to find
     * @return The found book club entity or null
     * @throws BookClubNotFoundException if the book club is not found, or if it is private and the reader is not a member
     * @throws ReaderNotFoundException if the book club is private and reader is not logged in
     */
    @Override
    public BookClubDTO findByID(UUID id) throws BookClubNotFoundException, ReaderNotFoundException {
        // Get the book club by ID
        BookClub bookClub = bookClubRepo.findById(id).orElseThrow(() -> new BookClubNotFoundException(id));

        // If the book club is not private, return it
        if(bookClub.getPublicity() != Publicity.PRIVATE) {
            return bookClubMapper.entityToDTO(bookClub);
        }

        // Otherwise, check if the current reader is a member of the book club
        return checkBookClubMembership(bookClub);
    }

    /**
     * Find a book club by its name
     * @param name - The name of the book club to find
     * @return The found book club
     * @throws BookClubNotFoundException if the book club is not found, or if it is private and the reader is not a member
     * @throws ReaderNotFoundException if the book club is private and reader is not logged in
     */
    @Override
    public BookClubDTO findByName(String name) throws BookClubNotFoundException, ReaderNotFoundException {
        // Get the book club by name
        BookClub bookClub = bookClubRepo.findByName(name).orElseThrow(() -> new BookClubNotFoundException(name));

        // If the book club is not private, return it
        if(bookClub.getPublicity() != Publicity.PRIVATE) {
            return bookClubMapper.entityToDTO(bookClub);
        }

        // Otherwise, check if the current reader is a member of the book club
        return checkBookClubMembership(bookClub);
    }
    
    @Override
    public List<BookClubDTO> findAllForReader() {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException();
        }

        return bookClubMapper.entityListToDTO(bookClubRepo.findAllForReader(reader.getId()));
    }

    /**
     * Find all book clubs
     * @return A list of all book clubs
     */
    @Override
    public List<BookClubDTO> findAll() {
        return bookClubMapper.entityListToDTO(bookClubRepo.findAll());
    }

    /**
     * Disband a book club
     * @param id The ID of the book club to disband
     * @return The disbanded book club's persisted entity
     */
    @Override
    public BookClubDTO disbandBookClubByID(UUID id) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException();
        }

        // Find the reader's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
            .findByBookClubIdAndReaderId(id, reader.getId())
            .orElseThrow(() -> new MembershipNotFoundException(reader.getId(), id));

        return disbandBookClub(membership);
    }

    /**
     * Disband a book club
     * @param name The name of the book club to disband
     * @return The disbanded book club's persisted entity
     */
    @Override
    public BookClubDTO disbandBookClubByName(String name) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException();
        }

        // Find the reader's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
            .findByBookClubNameAndReaderId(name, reader.getId())
            .orElseThrow(() -> new MembershipNotFoundException(reader.getUsername(), name));

        return disbandBookClub(membership);
    }

    /**
     * Search for book clubs by name
     * @param searchTerm The name to search for
     * @return A list of book clubs that match the search term
     */
    @Override
    public List<BookClubDTO> search(String searchTerm) {
        return bookClubMapper.entityListToDTO(bookClubRepo.findAllByPublicityNotAndNameContainsIgnoreCase(Publicity.PRIVATE, searchTerm));
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
            throw new ReaderNotFoundException("Not logged in or reader not found");
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
            throw new ReaderNotFoundException("Not logged in or reader not found");
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
     * Ensure a reader is a member of a book club before returning the book club
     * @param bookClub The book club to check
     * @return The book club if the reader is a member
     * @throws BookClubNotFoundException if the reader is not a member of the book club
     * @throws ReaderNotFoundException if the reader is not logged in
     */
    private BookClubDTO checkBookClubMembership(BookClub bookClub) {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException();
        }

        // Check if the reader is a member of the book club
        if(!bookClubMembershipRepo.existsByBookClubIdAndReaderId(bookClub.getId(), reader.getId())) {
            throw new BookClubNotFoundException(bookClub.getId());
        }

        return bookClubMapper.entityToDTO(bookClub);
    }

    /**
     * Disband a book club
     * @param membership The membership of the reader in the book club to disband
     */
    private BookClubDTO disbandBookClub(BookClubMembership membership) {
        // Ensure the reader is the creator of the book club
        if(!membership.isCreator()) {
            throw new UnauthorizedBookClubActionException();
        }

        // Ensure the book club is not already disbanded
        BookClub bookClub = membership.getBookClub();
        if(bookClub.getDisbanded() != null) {
            throw new BadBookClubActionException();
        }

        // Disband the book club
        bookClub.setDisbanded(LocalDateTime.now());
        return bookClubMapper.entityToDTO(bookClubRepo.save(bookClub));
    }
}
