package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.BookClubNotFoundException;
import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.Notification;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.enums.NotificationType;
import com.bahubba.bahubbabookclub.model.mapper.BookClubMapper;
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
            throw new ReaderNotFoundException("Not logged in or reader not found");
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
            throw new ReaderNotFoundException("Not logged in or reader not found");
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
     */
    @Override
    public BookClubDTO findByID(UUID id) {
        return bookClubMapper.entityToDTO(
            bookClubRepo.findById(id).orElseThrow(() -> new BookClubNotFoundException(id))
        );
    }

    /**
     * Find a book club by its name
     * @param name - The name of the book club to find
     * @return The found book club
     * @throws BookClubNotFoundException if the book club is not found
     */
    @Override
    public BookClubDTO findByName(String name) {
        return bookClubMapper.entityToDTO(
            bookClubRepo.findByName(name).orElseThrow(() -> new BookClubNotFoundException(name))
        );
    }
    
    @Override
    public List<BookClubDTO> findAllForReader() {
        // Get the current reader from the security context
        Reader reader = SecurityUtil.getCurrentUserDetails();
        if(reader == null) {
            throw new ReaderNotFoundException("Not logged in or reader not found");
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
     * @return The disbanded book club's persisted entity
     */
    @Override
    public BookClubDTO disbandBookClub(UUID id) {
        BookClub bookClub = bookClubRepo.findById(id).orElseThrow(() -> new BookClubNotFoundException(id));
        bookClub.setDisbanded(LocalDateTime.now());
        return bookClubMapper.entityToDTO(bookClubRepo.save(bookClub));
    }
}
