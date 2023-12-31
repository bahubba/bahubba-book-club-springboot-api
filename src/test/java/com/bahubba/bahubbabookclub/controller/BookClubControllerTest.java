package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.exception.BookClubNotFoundException;
import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.payload.BookClubSearch;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import com.bahubba.bahubbabookclub.service.BookClubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BookClubController} endpoints
 */
@SpringBootTest
@ActiveProfiles("test")
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
    void testUpdate() {
        when(bookClubService.update(any(BookClubDTO.class))).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.update(new BookClubDTO());
        verify(bookClubService, times(1)).update(any(BookClubDTO.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
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
    void testGetByName() {
        when(bookClubService.findByName(anyString())).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.getByName("foo");
        verify(bookClubService, times(1)).findByName(anyString());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetByName_Unauthorized() {
        when(bookClubService.findByName(anyString())).thenThrow(new ReaderNotFoundException());
        ResponseEntity<BookClubDTO> rsp = bookClubController.getByName("foo");
        verify(bookClubService, times(1)).findByName(anyString());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetByName_BookClubNotFound() {
        when(bookClubService.findByName(anyString())).thenThrow(new BookClubNotFoundException("foo"));
        ResponseEntity<BookClubDTO> rsp = bookClubController.getByName("foo");
        verify(bookClubService, times(1)).findByName(anyString());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetAllForReader() {
        when(bookClubService.findAllForReader(anyInt(), anyInt())).thenReturn(Page.empty());
        ResponseEntity<Page<BookClubDTO>> rsp = bookClubController.getAllForReader(1, 1);
        verify(bookClubService, times(1)).findAllForReader(anyInt(), anyInt());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetAll() {
        when(bookClubService.findAll(anyInt(), anyInt())).thenReturn(Page.empty());
        ResponseEntity<Page<BookClubDTO>> rsp = bookClubController.getAll(1, 1);
        verify(bookClubService, times(1)).findAll(anyInt(), anyInt());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testDisbandBookClub() {
        when(bookClubService.disbandBookClubByID(any(UUID.class))).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.disbandBookClub(UUID.randomUUID());
        verify(bookClubService, times(1)).disbandBookClubByID(any(UUID.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testDisbandBookClubByName() {
        when(bookClubService.disbandBookClubByName(anyString())).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.disbandBookClubByName("foo");
        verify(bookClubService, times(1)).disbandBookClubByName(anyString());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testSearch() {
        when(bookClubService.search(anyString(), anyInt(), anyInt())).thenReturn(Page.empty());
        ResponseEntity<Page<BookClubDTO>> rsp = bookClubController.search(
            BookClubSearch.builder().searchTerm("foo").pageNum(1).pageSize(1).build()
        );
        verify(bookClubService, times(1)).search(anyString(), anyInt(), anyInt());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }
}
