package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.payload.NewReader;

import java.util.List;
import java.util.UUID;

public interface ReaderService {
    public ReaderDTO create(NewReader newReader);

    public ReaderDTO findByID(UUID id);

    public List<ReaderDTO> findAll();

    public ReaderDTO removeReader(UUID id);
}
