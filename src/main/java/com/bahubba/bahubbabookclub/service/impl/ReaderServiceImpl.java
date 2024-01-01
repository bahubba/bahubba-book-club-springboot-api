package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.mapper.ReaderMapper;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import com.bahubba.bahubbabookclub.service.ReaderService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {

    private final ReaderRepo readerRepo;
    private final ReaderMapper readerMapper;

    @Override
    public ReaderDTO findByID(UUID id) throws ReaderNotFoundException {
        return readerMapper.entityToDTO(readerRepo.findById(id).orElseThrow(() -> new ReaderNotFoundException(id)));
    }

    // TODO - Convert to pagination
    @Override
    public List<ReaderDTO> findAll() {
        return readerMapper.entityListToDTO(readerRepo.findAll());
    }

    // FIXME - Need to ensure the user is removing themself
    @Override
    public ReaderDTO removeReader(UUID id) throws ReaderNotFoundException {
        Reader reader = readerRepo.findById(id).orElseThrow(() -> new ReaderNotFoundException(id));
        reader.setDeparted(LocalDateTime.now());
        return readerMapper.entityToDTO(readerRepo.save(reader));
    }
}
