package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.service.ReaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Reader (user) endpoints
 */
@RestController
@RequestMapping("/api/v1/readers")
@Tag(name = "Reader Controller", description = "Reader (user) endpoints")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderService readerService;

    /**
     * Retrieve a reader (user) by ID
     * @param id The reader (user) ID
     * @return The reader (user) info
     * @throws ReaderNotFoundException The reader was not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Reader by ID", description = "Retrieves a reader (user) by ID")
    public ResponseEntity<ReaderDTO> getByID(@PathVariable UUID id) throws ReaderNotFoundException {
        return ResponseEntity.ok(readerService.findByID(id));
    }

    /**
     * Retrieves all readers (users)
     * @return All readers (users)
     */
    @GetMapping("/all")
    @Operation(summary = "Get All Readers", description = "Retrieves all readers (users)")
    public ResponseEntity<List<ReaderDTO>> getAll() {
        return ResponseEntity.ok(readerService.findAll());
    }

    /**
     * Removes (soft deletes) reader (user)
     * @param id The reader (user) ID
     * @return Persisted data from the soft deleted reader (user)
     * @throws ReaderNotFoundException The reader was not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove Reader", description = "Removes (soft deletes) a reader (user)")
    public ResponseEntity<ReaderDTO> removeReader(@PathVariable UUID id) throws ReaderNotFoundException {
        return ResponseEntity.ok(readerService.removeReader(id));
    }
}
