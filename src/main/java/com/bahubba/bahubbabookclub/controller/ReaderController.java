package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Reader (user) endpoints
 */
@RestController
@RequestMapping("/api/v1/readers")
public class ReaderController {
    @Autowired
    private ReaderService readerService;

    /**
     * Creates a new reader (user)
     * @param newReader metadata for a new reader
     * @return persisted reader (user) info
     * @deprecated (use {@link com.bahubba.bahubbabookclub.controller.AuthController#register()} instead
     */
    @Deprecated
    @PostMapping("/create")
    public ResponseEntity<ReaderDTO> create(@RequestBody NewReader newReader) {
        return ResponseEntity.ok(readerService.create(newReader));
    }

    /**
     * Retrieves a reader (user) by ID
     * @param id reader (user) ID
     * @return persisted reader info
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReaderDTO> getByID(@PathVariable UUID id) {
        return ResponseEntity.ok(readerService.findByID(id));
    }

    /**
     * Retrieves all readers (users)
     * @return all readers (users)
     */
    @GetMapping("/all")
    public ResponseEntity<List<ReaderDTO>> getAll() {
        return ResponseEntity.ok(readerService.findAll());
    }

    /**
     * Removes (soft deletes) reader (user)
     * @param id reader (user) ID
     * @return persisted data from the soft deleted reader (user)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ReaderDTO> removeReader(@PathVariable UUID id) {
        return ResponseEntity.ok(readerService.removeReader(id));
    }
}
