package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/readers")
public class ReaderController {
    @Autowired
    private ReaderService readerService;

    @PostMapping("/create")
    public ResponseEntity<ReaderDTO> create(@RequestBody NewReader newReader) {
        return ResponseEntity.ok(readerService.create(newReader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReaderDTO> getByID(@PathVariable UUID id) {
        return ResponseEntity.ok(readerService.findByID(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReaderDTO>> getAll() {
        return ResponseEntity.ok(readerService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReaderDTO> removeReader(@PathVariable UUID id) {
        return ResponseEntity.ok(readerService.removeReader(id));
    }
}
