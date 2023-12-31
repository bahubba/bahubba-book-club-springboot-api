package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
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
@ActiveProfiles("test")
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
    void testCreate_NoReader() {
        assertThrows(ReaderNotFoundException.class, () -> bookClubService.create(new NewBookClub()));
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
    void testUpdate_NoReader() {
        assertThrows(ReaderNotFoundException.class, () -> bookClubService.update(new BookClubDTO()));
    }

    @Test
    void testFindByID() {
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.of(BookClub.builder().publicity(Publicity.PUBLIC).build()));
        BookClubDTO result = bookClubService.findByID(UUID.randomUUID());
        verify(bookClubRepo, times(1)).findById(any(UUID.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testFindByID_Private() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.of(BookClub.builder().id(UUID.randomUUID()).publicity(Publicity.PRIVATE).build()));
        when(bookClubMembershipRepo.existsByBookClubIdAndReaderId(any(UUID.class), any(UUID.class))).thenReturn(true);
        BookClubDTO result = bookClubService.findByID(UUID.randomUUID());
        verify(bookClubRepo, times(1)).findById(any(UUID.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testFindByID_NotFound() {
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(BookClubNotFoundException.class, () -> bookClubService.findByID(UUID.randomUUID()));
    }

    @Test
    void testFindByName() {
        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.of(BookClub.builder().publicity(Publicity.PUBLIC).build()));
        BookClubDTO result = bookClubService.findByName("foo");
        verify(bookClubRepo, times(1)).findByName(anyString());
        assertThat(result).isNotNull();
    }

    @Test
    void testFindByName_Private() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.of(BookClub.builder().id(UUID.randomUUID()).publicity(Publicity.PRIVATE).build()));
        when(bookClubMembershipRepo.existsByBookClubIdAndReaderId(any(UUID.class), any(UUID.class))).thenReturn(true);
        BookClubDTO result = bookClubService.findByName("foo");
        verify(bookClubRepo, times(1)).findByName(anyString());
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testFindByName_NotFound() {
        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.empty());
        assertThrows(BookClubNotFoundException.class, () -> bookClubService.findByName("foo"));
    }

    @Test
    void testFindAllForReader() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findAllForReader(any(UUID.class), any(Pageable.class))).thenReturn(Page.empty());
        Page<BookClubDTO> result = bookClubService.findAllForReader(1, 1);
        verify(bookClubRepo, times(1)).findAllForReader(any(UUID.class), any(Pageable.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testFindAllForReader_NoReader() {
        assertThrows(ReaderNotFoundException.class, () -> bookClubService.findAllForReader(1, 1));
    }

    @Test
    void testFindAllForReader_NegativePageSize() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findAllForReader(any(UUID.class), any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(PageSizeTooSmallException.class, () -> bookClubService.findAllForReader(1, -1));
        verify(bookClubRepo, times(1)).findAllForReader(any(UUID.class), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testFindAllForReader_TooLargePageSize() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findAllForReader(any(UUID.class), any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(PageSizeTooLargeException.class, () -> bookClubService.findAllForReader(1, 51));
        verify(bookClubRepo, times(1)).findAllForReader(any(UUID.class), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testFindAll() {
        when(bookClubRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());
        Page<BookClubDTO> result = bookClubService.findAll(1, 1);
        verify(bookClubRepo, times(1)).findAll(any(Pageable.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testFindAll_NegativePageSize() {
        when(bookClubRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(PageSizeTooSmallException.class, () -> bookClubService.findAll(1, -1));
        verify(bookClubRepo, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testFindAll_TooLargePageSize() {
        when(bookClubRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(PageSizeTooLargeException.class, () -> bookClubService.findAll(1, 51));
        verify(bookClubRepo, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testDisbandBookClubByID() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(testID).build());
        when(bookClubMembershipRepo.findByBookClubIdAndReaderId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(
            BookClubMembership
                .builder()
                .reader(Reader.builder().id(testID).build())
                .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                .isOwner(true)
                .build()
        ));
        when(bookClubRepo.save(any(BookClub.class))).thenReturn(new BookClub());

        BookClubDTO result = bookClubService.disbandBookClubByID(UUID.randomUUID());

        verify(bookClubMembershipRepo, times(1)).findByBookClubIdAndReaderId(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(1)).save(any(BookClub.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClubByID_NoReader() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(ReaderNotFoundException.class, () -> bookClubService.disbandBookClubByID(UUID.randomUUID()));
        verify(bookClubMembershipRepo, times(0)).findByBookClubIdAndReaderId(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClubByID_MembershipNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());
        when(bookClubMembershipRepo.findByBookClubIdAndReaderId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class, () -> bookClubService.disbandBookClubByID(UUID.randomUUID()));

        verify(bookClubMembershipRepo, times(1)).findByBookClubIdAndReaderId(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClub_ReaderNotOwner() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(testID).build());

        when(bookClubMembershipRepo.findByBookClubIdAndReaderId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(
            BookClubMembership
                .builder()
                .reader(Reader.builder().id(testID).build())
                .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                .isOwner(false)
                .build()
        ));

        assertThrows(UnauthorizedBookClubActionException.class, () -> bookClubService.disbandBookClubByID(UUID.randomUUID()));
        verify(bookClubMembershipRepo, times(1)).findByBookClubIdAndReaderId(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClub_AlreadyDisbanded() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(testID).build());
        when(bookClubMembershipRepo.findByBookClubIdAndReaderId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(
            BookClubMembership
                .builder()
                .reader(Reader.builder().id(testID).build())
                .bookClub(BookClub.builder().id(UUID.randomUUID()).disbanded(LocalDateTime.now()).build())
                .isOwner(true)
                .build()
        ));

        assertThrows(BadBookClubActionException.class, () -> bookClubService.disbandBookClubByID(UUID.randomUUID()));

        verify(bookClubMembershipRepo, times(1)).findByBookClubIdAndReaderId(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));
        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClubByName() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(testID).build());
        when(bookClubMembershipRepo.findByBookClubNameAndReaderId(anyString(), any(UUID.class))).thenReturn(Optional.of(
            BookClubMembership
                .builder()
                .reader(Reader.builder().id(testID).build())
                .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                .isOwner(true)
                .build()
        ));
        when(bookClubRepo.save(any(BookClub.class))).thenReturn(new BookClub());

        BookClubDTO result = bookClubService.disbandBookClubByName("foo");

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndReaderId(anyString(), any(UUID.class));
        verify(bookClubRepo, times(1)).save(any(BookClub.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClubByName_NoReader() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(ReaderNotFoundException.class, () -> bookClubService.disbandBookClubByName("foo"));
        verify(bookClubMembershipRepo, times(0)).findByBookClubNameAndReaderId(anyString(), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClubByName_MembershipNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());
        when(bookClubMembershipRepo.findByBookClubNameAndReaderId(anyString(), any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class, () -> bookClubService.disbandBookClubByName("foo"));

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndReaderId(anyString(), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testSearch() {
        when(bookClubRepo.findAllByPublicityNotAndNameContainsIgnoreCase(
            any(Publicity.class),
            anyString(),
            any(Pageable.class)
        )).thenReturn(Page.empty());

        Page<BookClubDTO> result = bookClubService.search("foo", 1, 1);

        verify(bookClubRepo, times(1)).findAllByPublicityNotAndNameContainsIgnoreCase(
            any(Publicity.class),
            anyString(),
            any(Pageable.class)
        );
        assertThat(result).isNotNull();
    }

    @Test
    void testSearch_NegativePageSize() {
        when(bookClubRepo.findAllByPublicityNotAndNameContainsIgnoreCase(
            any(Publicity.class),
            anyString(),
            any(Pageable.class)
        )).thenReturn(Page.empty());

        assertThrows(PageSizeTooSmallException.class, () -> bookClubService.search("foo", 1, -1));
        verify(bookClubRepo, times(1)).findAllByPublicityNotAndNameContainsIgnoreCase(
            any(Publicity.class),
            anyString(),
            any(Pageable.class)
        );
    }

    @Test
    void testSearch_TooLargePageSize() {
        when(bookClubRepo.findAllByPublicityNotAndNameContainsIgnoreCase(
            any(Publicity.class),
            anyString(),
            any(Pageable.class)
        )).thenReturn(Page.empty());

        assertThrows(PageSizeTooLargeException.class, () -> bookClubService.search("foo", 1, 51));
        verify(bookClubRepo, times(1)).findAllByPublicityNotAndNameContainsIgnoreCase(
            any(Publicity.class),
            anyString(),
            any(Pageable.class)
        );
    }

    @Test
    void testCheckBookClubMembership_NoReader() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.of(BookClub.builder().publicity(Publicity.PRIVATE).build()));

        assertThrows(ReaderNotFoundException.class, () -> bookClubService.findByID(UUID.randomUUID()));

        securityUtilMockedStatic.close();
    }

    @Test
    void testCheckBookClubMembership_NoMembership() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.of(BookClub.builder().publicity(Publicity.PRIVATE).build()));
        when(bookClubMembershipRepo.existsByBookClubIdAndReaderId(any(UUID.class), any(UUID.class))).thenReturn(false);

        assertThrows(BookClubNotFoundException.class, () -> bookClubService.findByID(UUID.randomUUID()));

        securityUtilMockedStatic.close();
    }
}
