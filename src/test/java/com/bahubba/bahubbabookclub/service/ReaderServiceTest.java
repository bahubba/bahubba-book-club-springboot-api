package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ReaderServiceTest {
    @Autowired
    ReaderService readerService;

    @MockBean
    ReaderRepo readerRepo;

    @Test
    void testCreate() {
        when(readerRepo.save(any(Reader.class))).thenReturn(new Reader());
        ReaderDTO result = readerService.create(NewReader.builder().password("password").build());
        verify(readerRepo, times(1)).save(any(Reader.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testFindByID() {
        when(readerRepo.findById(any(UUID.class))).thenReturn(Optional.of(new Reader()));
        ReaderDTO result = readerService.findByID(UUID.randomUUID());
        verify(readerRepo, times(1)).findById(any(UUID.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testFindByID_NotFound() {
        when(readerRepo.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ReaderNotFoundException.class, () -> readerService.findByID(UUID.randomUUID()));
    }

    @Test
    void testFindAll() {
        when(readerRepo.findAll()).thenReturn(new ArrayList<>(List.of(new Reader())));
        List<ReaderDTO> result = readerService.findAll();
        verify(readerRepo, times(1)).findAll();
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    void testRemoveReader() {
        when(readerRepo.findById(any(UUID.class))).thenReturn(Optional.of(new Reader()));
        when(readerRepo.save(any(Reader.class))).thenReturn(new Reader());
        ReaderDTO result = readerService.removeReader(UUID.randomUUID());
        verify(readerRepo, times(1)).findById(any(UUID.class));
        verify(readerRepo, times(1)).save(any(Reader.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testRemoveReader_ReaderNotFound() {
        when(readerRepo.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ReaderNotFoundException.class, () -> readerService.removeReader(UUID.randomUUID()));
    }
}
