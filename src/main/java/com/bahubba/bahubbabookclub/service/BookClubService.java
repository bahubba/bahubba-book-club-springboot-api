package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * {@link BookClub} service layer
 */
public interface BookClubService {

    /**
     * Create a new book club
     * @param newBookClub Metadata for the new book club
     * @return The new book club's persisted entity
     * @throws ReaderNotFoundException The reader was not found
     */
    BookClubDTO create(NewBookClub newBookClub) throws ReaderNotFoundException;

    /**
     * Update a book club
     * @param bookClubDTO The book club's new metadata
     * @return The updated book club's persisted entity
     * @throws ReaderNotFoundException The reader was not found
     * @throws BookClubNotFoundException The book club was not found
     */
    BookClubDTO update(BookClubDTO bookClubDTO) throws ReaderNotFoundException, BookClubNotFoundException;

    /**
     * Find a book club by its ID
     * @param id The ID of the book club to find
     * @return The found book club entity or null
     * @throws BookClubNotFoundException The book club was not found, or it was private and the reader was not a member
     * @throws ReaderNotFoundException The book club was private or reader was not logged in
     * @throws MembershipNotFoundException The reader was not a member of the book club
     */
    BookClubDTO findByID(UUID id) throws BookClubNotFoundException, ReaderNotFoundException, MembershipNotFoundException;

    /**
     * Find a book club by its name
     * @param name The name of the book club to find
     * @return The found book club
     * @throws BookClubNotFoundException The book club was not found, or it was private and the reader was not a member
     * @throws ReaderNotFoundException The book club was private or reader was not logged in
     * @throws MembershipNotFoundException The reader was not a member of the book club
     */
    BookClubDTO findByName(String name)
        throws  BookClubNotFoundException, ReaderNotFoundException, MembershipNotFoundException;

    /**
     * Finds book clubs that the reader has some role in
     * @param pageNum Page number for results
     * @param pageSize Number of results per page
     * @return A page of book clubs that the reader has some role in
     * @throws ReaderNotFoundException The reader wasn't found in the DB
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    Page<BookClubDTO> findAllForReader(int pageNum, int pageSize)
        throws ReaderNotFoundException, PageSizeTooSmallException, PageSizeTooLargeException;

    /**
     * Find all book clubs
     * @return A page of results of all book clubs
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    Page<BookClubDTO> findAll(int pageNum, int pageSize) throws PageSizeTooSmallException, PageSizeTooLargeException;

    /**
     * Search for book clubs by name
     * @param searchTerm The name to search for
     * @return A paged list of book clubs that match the search term
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    Page<BookClubDTO> search(String searchTerm, int pageNum, int pageSize)
        throws PageSizeTooSmallException, PageSizeTooLargeException;

    /**
     * Disband a book club
     * @param id The ID of the book club to disband
     * @return The disbanded book club's persisted entity
     * @throws ReaderNotFoundException The reader was not found
     * @throws MembershipNotFoundException The reader was not a member of the book club
     * @throws UnauthorizedBookClubActionException The reader was not the owner of the book club
     * @throws BadBookClubActionException The book club was already disbanded
     */
    BookClubDTO disbandBookClubByID(UUID id)
        throws ReaderNotFoundException,
        MembershipNotFoundException,
        UnauthorizedBookClubActionException,
        BadBookClubActionException;

    /**
     * Disband a book club
     * @param name The name of the book club to disband
     * @return The disbanded book club's persisted entity
     * @throws ReaderNotFoundException The reader was not found
     * @throws MembershipNotFoundException The reader was not a member of the book club
     * @throws UnauthorizedBookClubActionException The reader was not the owner of the book club
     * @throws BadBookClubActionException The book club was already disbanded
     */
    BookClubDTO disbandBookClubByName(String name)
        throws ReaderNotFoundException,
        MembershipNotFoundException,
        UnauthorizedBookClubActionException,
        BadBookClubActionException;
}
