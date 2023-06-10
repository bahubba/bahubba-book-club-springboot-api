package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import com.bahubba.bahubbabookclub.service.BookClubService;
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

@SpringBootTest
class BookClubControllerTest {
    @Autowired
    BookClubController bookClubController;

    @MockBean
    BookClubService bookClubService;

    @Test
    void testCreate() {
        when(bookClubService.create(any(NewBookClub.class))).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.create(new NewBookClub());
        verify(bookClubService, times(1)).create(any(NewBookClub.class));
        assertThat(rsp).isNotNull();
    }

    @Test
    void testGetByID() {
        when(bookClubService.findByID(any(UUID.class))).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.getByID(UUID.randomUUID());
        verify(bookClubService, times(1)).findByID(any(UUID.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetAll() {
        when(bookClubService.findAll()).thenReturn(new ArrayList<>());
        ResponseEntity<List<BookClubDTO>> rsp = bookClubController.getAll();
        verify(bookClubService, times(1)).findAll();
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testDisbandBookClub() {
        when(bookClubService.disbandBookClub(any(UUID.class))).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.disbandBookClub(UUID.randomUUID());
        verify(bookClubService, times(1)).disbandBookClub(any(UUID.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }
}
