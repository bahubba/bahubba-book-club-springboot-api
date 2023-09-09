package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.exception.BookClubNotFoundException;
import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.BookClubSearch;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import com.bahubba.bahubbabookclub.service.BookClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Book Club endpoints
 */
@RestController
@RequestMapping("/api/v1/book-clubs")
public class BookClubController {
    @Autowired
    private BookClubService bookClubService;

    /**
     * Creates a book club
     * @param newBookClub metadata for a new book club
     * @return persisted version of the new book club
     */
    @PostMapping("/create")
    public ResponseEntity<BookClubDTO> create(@RequestBody NewBookClub newBookClub) {
        return ResponseEntity.ok(bookClubService.create(newBookClub));
    }

    /**
     * Updates a book club
     * @param bookClub new book club metadata
     * @return persisted version of the new book club
     */
    @PatchMapping("/update")
    public ResponseEntity<BookClubDTO> update(@RequestBody BookClubDTO bookClub) {
        return ResponseEntity.ok(bookClubService.update(bookClub));
    }

    /**
     * Retrieves a book club by ID
     * @param id book club ID
     * @return a book club
     */
    @GetMapping("/by-id/{id}")
    public ResponseEntity<BookClubDTO> getByID(@PathVariable  UUID id) {
        return ResponseEntity.ok(bookClubService.findByID(id));
    }

    /**
     * Retrieves a book club by name
     * @param name book club name
     * @return a book club
     */
    @GetMapping("/by-name/{name}")
    public ResponseEntity<BookClubDTO> getByName(@PathVariable String name) {
        try {
            return ResponseEntity.ok(bookClubService.findByName(name));
        } catch (ReaderNotFoundException rnfe) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (BookClubNotFoundException bcnfe) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Retrieves all book clubs for a given reader
     * @return all book clubs that the requesting reader has a role in
     */
    @GetMapping("/all-for-reader")
    public ResponseEntity<List<BookClubDTO>> getAllForReader() {
        return ResponseEntity.ok(bookClubService.findAllForReader());
    }

    /**
     * Retrieves all book clubs
     * @return persisted version of the new book club
     */
    // TODO - pre-authorize this endpoint to only allow admins to access it
    @GetMapping("/all")
    public ResponseEntity<List<BookClubDTO>> getAll() {
        return ResponseEntity.ok(bookClubService.findAll());
    }

    /**
     * Disbands (soft deletes) a book club
     * @param id book club ID
     * @return persisted version of the new book club
     */
    @DeleteMapping("/disband/{id}")
    public ResponseEntity<BookClubDTO> disbandBookClub(@PathVariable UUID id) {
        return ResponseEntity.ok(bookClubService.disbandBookClubByID(id));
    }

    /**
     * Disbands (soft deletes) a book club by name
     * @param name book club name
     * @return persisted version of the new book club
     */
    @DeleteMapping("/disband-by-name/{name}")
    public ResponseEntity<BookClubDTO> disbandBookClubByName(@PathVariable String name) {
        return ResponseEntity.ok(bookClubService.disbandBookClubByName(name));
    }

    /**
     * Searches for book clubs by name
     * @param bookClubSearch search term and pagination info
     * @return list of book clubs
     */
    @PostMapping(value = "/search")
    public ResponseEntity<List<BookClubDTO>> search(@RequestBody BookClubSearch bookClubSearch) {
        return ResponseEntity.ok(bookClubService.search(bookClubSearch.getSearchTerm()));
    }

    /**
     * Gets user's role in a book club
     * @param bookClubName name of the book club
     * @return user's role in the book club
     */
    @GetMapping("/role/{bookClubName}")
    public ResponseEntity<BookClubRole> getRole(@PathVariable String bookClubName) {
        return ResponseEntity.ok(bookClubService.getRole(bookClubName));
    }

    /**
     * Get a non-private book club and the user's role (or lack thereof) in it
     * @param bookClubName name of the book club
     * @return book club and user's role in it
     */
    @GetMapping("/membership/{bookClubName}")
    public ResponseEntity<BookClubMembershipDTO> getMembership(@PathVariable String bookClubName) {
        return ResponseEntity.ok(bookClubService.getMembership(bookClubName));
    }
}
