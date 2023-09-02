package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.repository.MembershipRequestRepo;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class MembershipRequestServiceTest {
    @Autowired
    MembershipRequestService membershipRequestService;

    @MockBean
    MembershipRequestRepo membershipRequestRepo;

    @MockBean
    BookClubRepo bookClubRepo;

    @Test
    void testRequestMembership() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(new Reader());
        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.of(new BookClub()));
        when(membershipRequestRepo.save(any(MembershipRequest.class))).thenReturn(new MembershipRequest());

        MembershipRequestDTO result = membershipRequestService.requestMembership(NewMembershipRequest.builder().bookClubName("foo").build());

        verify(bookClubRepo, times(1)).findByName(anyString());
        verify(membershipRequestRepo, times(1)).save(any(MembershipRequest.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testRequestMembership_ReaderNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(ReaderNotFoundException.class, () -> {
            membershipRequestService.requestMembership(NewMembershipRequest.builder().bookClubName("foo").build());
        });

        securityUtilMockedStatic.close();
    }

    @Test
    void testHasPendingRequest() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());
        when(membershipRequestRepo.existsByBookClubNameAndReaderIdAndStatusIn(
            anyString(),
            any(UUID.class),
            anyList()
        )).thenReturn(true);
        Boolean result = membershipRequestService.hasPendingRequest("foo");
        verify(membershipRequestRepo, times(1)).existsByBookClubNameAndReaderIdAndStatusIn(
            anyString(),
            any(UUID.class),
            anyList()
        );
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testHasPendingRequest_ReaderNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(ReaderNotFoundException.class, () -> {
            membershipRequestService.hasPendingRequest("foo");
        });

        securityUtilMockedStatic.close();
    }
}
