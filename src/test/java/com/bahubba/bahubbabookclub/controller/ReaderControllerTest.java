package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.service.ReaderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
class ReaderControllerTest {
    @Autowired
    ReaderController readerController;

    @MockBean
    ReaderService readerService;

    @Test
    void testCreate() {
        when(readerService.create(any(NewReader.class))).thenReturn(new ReaderDTO());
        ResponseEntity<ReaderDTO> rsp = readerController.create(new NewReader());
        verify(readerService, times(1)).create(any(NewReader.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetByID() {
        when(readerService.findByID(any(UUID.class))).thenReturn(new ReaderDTO());
        ResponseEntity<ReaderDTO> rsp = readerController.getByID(UUID.randomUUID());
        verify(readerService, times(1)).findByID(any(UUID.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetAll() {
        when(readerService.findAll()).thenReturn(new ArrayList<>());
        ResponseEntity<List<ReaderDTO>> rsp = readerController.getAll();
        verify(readerService, times(1)).findAll();
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testRemoveReader() {
        when(readerService.removeReader(any(UUID.class))).thenReturn(new ReaderDTO());
        ResponseEntity<ReaderDTO> rsp = readerController.removeReader(UUID.randomUUID());
        verify(readerService, times(1)).removeReader(any(UUID.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }
}
