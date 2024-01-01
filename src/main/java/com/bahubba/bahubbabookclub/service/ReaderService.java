package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.payload.NewReader;

import java.util.List;
import java.util.UUID;

public interface ReaderService {

    /**
     * Retrieve a reader by ID
     * @param id The reader's ID
     * @return The reader's info
     * @throws ReaderNotFoundException The reader was not found
     */
    public ReaderDTO findByID(UUID id) throws ReaderNotFoundException;

    /**
     * Retrieve all readers
     * @return All readers
     */
    // TODO - add pagination
    public List<ReaderDTO> findAll();

    /**
     * Remove (soft delete) a reader
     * @param id The reader's ID
     * @return The reader's updated info with a departure date
     * @throws ReaderNotFoundException The reader was not found
     */
    public ReaderDTO removeReader(UUID id) throws ReaderNotFoundException;
}
