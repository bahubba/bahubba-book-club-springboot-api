package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.BookClubNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.Notification;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.enums.Publicity;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.repository.NotificationRepo;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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

/**
 * Unit tests for the {@link BookClubService} business logic
 */
@SpringBootTest
class BookClubServiceTest {
    @Autowired
    BookClubService bookClubService;

    @MockBean
    BookClubRepo bookClubRepo;

    @MockBean
    BookClubMembershipRepo bookClubMembershipRepo;

    @MockBean
    NotificationRepo notificationRepo;

    @Test
    void testCreate() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(new Reader());
        when(bookClubRepo.save(any(BookClub.class))).thenReturn(new BookClub());
        BookClubDTO result = bookClubService.create(new NewBookClub());
        verify(bookClubRepo, times(1)).save(any(BookClub.class));
        verify(bookClubMembershipRepo, times(1)).save(any(BookClubMembership.class));
        verify(notificationRepo, times(1)).save(any(Notification.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdate() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(new Reader());
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.of(new BookClub()));
        when(bookClubRepo.save(any(BookClub.class))).thenReturn(new BookClub());

        BookClubDTO result = bookClubService.update(
            BookClubDTO
                .builder()
                .id(UUID.randomUUID())
                .name("foo")
                .description("bar")
                .imageURL("baz")
                .publicity(Publicity.PUBLIC)
                .build()
        );

        verify(bookClubRepo, times(1)).findById(any(UUID.class));
        verify(bookClubRepo, times(1)).save(any(BookClub.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testFindByID() {
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.of(new BookClub()));
        BookClubDTO result = bookClubService.findByID(UUID.randomUUID());
        verify(bookClubRepo, times(1)).findById(any(UUID.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testFindByID_NotFound() {
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(BookClubNotFoundException.class, () -> bookClubService.findByID(UUID.randomUUID()));
    }

    @Test
    void testFindByName() {
        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.of(new BookClub()));
        BookClubDTO result = bookClubService.findByName("foo");
        verify(bookClubRepo, times(1)).findByName(anyString());
        assertThat(result).isNotNull();
    }

    @Test
    void testFindByName_NotFound() {
        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.empty());
        assertThrows(BookClubNotFoundException.class, () -> bookClubService.findByName("foo"));
    }

    @Test
    void testFindAll() {
        when(bookClubRepo.findAll()).thenReturn(new ArrayList<>(List.of(new BookClub())));
        List<BookClubDTO> result = bookClubService.findAll();
        verify(bookClubRepo, times(1)).findAll();
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    void testDisbandBookClub() {
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.of(new BookClub()));
        when(bookClubRepo.save(any(BookClub.class))).thenReturn(new BookClub());
        BookClubDTO result = bookClubService.disbandBookClub(UUID.randomUUID());
        verify(bookClubRepo, times(1)).findById(any(UUID.class));
        verify(bookClubRepo, times(1)).save(any(BookClub.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testDisbandBookClub_BookClubNotFound() {
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(BookClubNotFoundException.class, () -> bookClubService.disbandBookClub(UUID.randomUUID()));
    }
}
