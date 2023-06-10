package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.mapper.ReaderMapper;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import com.bahubba.bahubbabookclub.service.ReaderService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReaderServiceImpl implements ReaderService {
    @Autowired
    private ReaderRepo readerRepo;

    @Autowired
    private ReaderMapper readerMapper;

    @Override
    public ReaderDTO create(NewReader newReader) {
        Reader newReaderEntity = readerMapper.modelToEntity(newReader);
        return readerMapper.entityToDTO(readerRepo.save(newReaderEntity));
    }

    @Override
    public ReaderDTO findByID(UUID id) {
        return readerMapper.entityToDTO(readerRepo.findById(id).orElseThrow(() -> new ReaderNotFoundException(id)));
    }

    @Override
    public List<ReaderDTO> findAll() {
        return readerMapper.entityListToDTO(readerRepo.findAll());
    }

    @Override
    public ReaderDTO removeReader(UUID id) {
        Reader reader = readerRepo.findById(id).orElseThrow(() -> new ReaderNotFoundException(id));
        reader.setDeparted(LocalDateTime.now());
        return readerMapper.entityToDTO(readerRepo.save(reader));
    }
}
