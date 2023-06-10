package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import com.bahubba.bahubbabookclub.service.BookClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/book-clubs")
public class BookClubController {
    @Autowired
    private BookClubService bookClubService;

    @PostMapping("/create")
    public ResponseEntity<BookClubDTO> create(@RequestBody NewBookClub newBookClub) {
        return ResponseEntity.ok(bookClubService.create(newBookClub));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookClubDTO> getByID(@PathVariable  UUID id) {
        return ResponseEntity.ok(bookClubService.findByID(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BookClubDTO>> getAll() {
        return ResponseEntity.ok(bookClubService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BookClubDTO> disbandBookClub(@PathVariable UUID id) {
        return ResponseEntity.ok(bookClubService.disbandBookClub(id));
    }
}
