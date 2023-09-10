package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.exception.BookClubNotFoundException;
import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.BookClubSearch;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import com.bahubba.bahubbabookclub.service.BookClubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(bookClubService.findAllForReader()).thenReturn(new ArrayList<>());
        ResponseEntity<List<BookClubDTO>> rsp = bookClubController.getAllForReader();
        verify(bookClubService, times(1)).findAllForReader();
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
        when(bookClubService.search(anyString())).thenReturn(new ArrayList<>());
        ResponseEntity<List<BookClubDTO>> rsp = bookClubController.search(BookClubSearch.builder().searchTerm("foo").build());
        verify(bookClubService, times(1)).search(anyString());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetMembers() {
        when(bookClubService.getMembers(anyString())).thenReturn(new ArrayList<>());
        ResponseEntity<List<BookClubMembershipDTO>> rsp = bookClubController.getMembers("foo");
        verify(bookClubService, times(1)).getMembers(anyString());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetRole() {
        when(bookClubService.getRole(anyString())).thenReturn(BookClubRole.READER);
        ResponseEntity<BookClubRole> rsp = bookClubController.getRole("foo");
        verify(bookClubService, times(1)).getRole(anyString());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetMembership() {
        when(bookClubService.getMembership(anyString())).thenReturn(BookClubMembershipDTO.builder().build());
        ResponseEntity<BookClubMembershipDTO> rsp = bookClubController.getMembership("foo");
        verify(bookClubService, times(1)).getMembership(anyString());
        assertThat(rsp).isNotNull();
    }
}
