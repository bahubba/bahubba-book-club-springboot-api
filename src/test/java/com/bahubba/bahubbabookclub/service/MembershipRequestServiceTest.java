package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.enums.RequestAction;
import com.bahubba.bahubbabookclub.model.enums.RequestStatus;
import com.bahubba.bahubbabookclub.model.payload.MembershipRequestAction;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.repository.MembershipRequestRepo;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @MockBean
    BookClubMembershipRepo bookClubMembershipRepo;

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
        when(membershipRequestRepo.existsByBookClubNameAndReaderIdAndStatus(
            anyString(),
            any(UUID.class),
            any(RequestStatus.class)
        )).thenReturn(true);

        Boolean result = membershipRequestService.hasPendingRequest("foo");

        verify(membershipRequestRepo, times(1)).existsByBookClubNameAndReaderIdAndStatus(
            anyString(),
            any(UUID.class),
            any(RequestStatus.class)
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

    @Test
    void testGetMembershipRequestsForBookClub() {
        var testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(testID).build());

        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.of(
            BookClub
                .builder()
                .id(UUID.randomUUID())
                .members(Set.of(
                    BookClubMembership
                        .builder()
                        .clubRole(BookClubRole.ADMIN)
                        .reader(
                            Reader
                                .builder()
                                .id(testID)
                                .build()
                        )
                        .build()
                ))
                .build()
        ));
        when(membershipRequestRepo.findALlByBookClubIdOrderByRequestedDesc(any(UUID.class))).thenReturn(
            List.of(
                MembershipRequest.builder().id(UUID.randomUUID()).build(),
                MembershipRequest.builder().id(UUID.randomUUID()).build()
            )
        );

        List<MembershipRequestDTO> result = membershipRequestService.getMembershipRequestsForBookClub("foo");

        verify(bookClubRepo, times(1)).findByName(anyString());
        verify(membershipRequestRepo, times(1)).findALlByBookClubIdOrderByRequestedDesc(any(UUID.class));
        assertThat(result).isNotNull().isNotEmpty();

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetMembershipRequestsForBookClub_ReaderNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(ReaderNotFoundException.class, () -> {
            membershipRequestService.getMembershipRequestsForBookClub("foo");
        });

        verify(bookClubRepo, times(0)).findByName(anyString());
        verify(membershipRequestRepo, times(0)).findALlByBookClubIdOrderByRequestedDesc(any(UUID.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetMembershipRequestsForBookClub_BookClubNotFound() {
        var testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
            .when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testID).build());

        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.empty());

        assertThrows(BookClubNotFoundException.class, () -> {
            membershipRequestService.getMembershipRequestsForBookClub("foo");
        });

        verify(bookClubRepo, times(1)).findByName(anyString());
        verify(membershipRequestRepo, times(0)).findALlByBookClubIdOrderByRequestedDesc(any(UUID.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetMembershipRequestsForBookClub_ReaderNotMember() {
        var testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(testID).build());

        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.of(
            BookClub
                .builder()
                .id(UUID.randomUUID())
                .members(Set.of(
                    BookClubMembership
                        .builder()
                        .clubRole(BookClubRole.ADMIN)
                        .reader(
                            Reader
                                .builder()
                                .id(UUID.randomUUID())
                                .build()
                        )
                        .build()
                ))
                .build()
        ));

        assertThrows(ReaderNotFoundException.class, () -> membershipRequestService.getMembershipRequestsForBookClub("foo"));

        verify(bookClubRepo, times(1)).findByName(anyString());
        verify(membershipRequestRepo, times(0)).findALlByBookClubIdOrderByRequestedDesc(any(UUID.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetMembershipRequestsForBookClub_ReaderNotAdmin() {
        var testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(testID).build());

        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.of(
            BookClub
                .builder()
                .id(UUID.randomUUID())
                .members(Set.of(
                    BookClubMembership
                        .builder()
                        .clubRole(BookClubRole.READER)
                        .reader(
                            Reader
                                .builder()
                                .id(testID)
                                .build()
                        )
                        .build()
                ))
                .build()
        ));

        assertThrows(UnauthorizedBookClubActionException.class, () -> membershipRequestService.getMembershipRequestsForBookClub("foo"));

        verify(bookClubRepo, times(1)).findByName(anyString());
        verify(membershipRequestRepo, times(0)).findALlByBookClubIdOrderByRequestedDesc(any(UUID.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testReviewMembershipRequest_Approve() {
        var testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(
            Reader
                .builder()
                .id(testID)
                .username("foo")
                .build()
        );
        when(membershipRequestRepo.findById(any(UUID.class))).thenReturn(Optional.of(
            MembershipRequest
                .builder()
                .id(UUID.randomUUID())
                .reader(
                    Reader
                        .builder()
                        .id(UUID.randomUUID())
                        .build()
                )
                .bookClub(
                    BookClub
                        .builder()
                        .members(Set.of(
                            BookClubMembership
                                .builder()
                                .clubRole(BookClubRole.ADMIN)
                                .reader(
                                    Reader
                                        .builder()
                                        .id(testID)
                                        .build()
                                )
                                .build()
                        ))
                        .build()
                )
                .status(RequestStatus.OPEN)
                .build()
        ));
        when(membershipRequestRepo.save(any(MembershipRequest.class))).thenReturn(new MembershipRequest());

        MembershipRequestDTO result = membershipRequestService.reviewMembershipRequest(
            MembershipRequestAction
                .builder()
                .role(BookClubRole.READER)
                .reviewMessage("bar")
                .action(RequestAction.APPROVE)
                .membershipRequest(
                    MembershipRequestDTO
                        .builder()
                        .id(UUID.randomUUID())
                        .reader(
                            ReaderDTO
                                .builder()
                                .id(UUID.randomUUID())
                                .username("foo")
                                .build()
                        )
                        .bookClub(
                            BookClubDTO
                                .builder()
                                .name("foo")
                                .build()
                        )
                        .build()
                )
                .build()
        );

        verify(membershipRequestRepo, times(1)).findById(any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).save(any(BookClubMembership.class));
        verify(membershipRequestRepo, times(1)).save(any(MembershipRequest.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testReviewMembershipRequest_Reject() {
        var testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(
            Reader
                .builder()
                .id(testID)
                .username("foo")
                .build()
        );
        when(membershipRequestRepo.findById(any(UUID.class))).thenReturn(Optional.of(
            MembershipRequest
                .builder()
                .id(UUID.randomUUID())
                .reader(
                    Reader
                        .builder()
                        .id(UUID.randomUUID())
                        .build()
                )
                .bookClub(
                    BookClub
                        .builder()
                        .members(Set.of(
                            BookClubMembership
                                .builder()
                                .clubRole(BookClubRole.ADMIN)
                                .reader(
                                    Reader
                                        .builder()
                                        .id(testID)
                                        .build()
                                )
                                .build()
                        ))
                        .build()
                )
                .status(RequestStatus.OPEN)
                .build()
        ));
        when(membershipRequestRepo.save(any(MembershipRequest.class))).thenReturn(new MembershipRequest());

        MembershipRequestDTO result = membershipRequestService.reviewMembershipRequest(
            MembershipRequestAction
                .builder()
                .role(BookClubRole.READER)
                .reviewMessage("bar")
                .action(RequestAction.REJECT)
                .membershipRequest(
                    MembershipRequestDTO
                        .builder()
                        .id(UUID.randomUUID())
                        .reader(
                            ReaderDTO
                                .builder()
                                .id(UUID.randomUUID())
                                .username("foo")
                                .build()
                        )
                        .bookClub(
                            BookClubDTO
                                .builder()
                                .name("foo")
                                .build()
                        )
                        .build()
                )
                .build()
        );

        verify(membershipRequestRepo, times(1)).findById(any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));
        verify(membershipRequestRepo, times(1)).save(any(MembershipRequest.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testReviewMembershipRequest_ReaderNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(ReaderNotFoundException.class, () -> {
            membershipRequestService.reviewMembershipRequest(MembershipRequestAction.builder().build());
        });

        securityUtilMockedStatic.close();
    }

    @Test
    void testReviewMembershipRequest_MembershipRequestNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(
            Reader
                .builder()
                .id(UUID.randomUUID())
                .username("foo")
                .build()
        );
        when(membershipRequestRepo.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(MembershipRequestNotFoundException.class, () -> {
            membershipRequestService.reviewMembershipRequest(
                MembershipRequestAction
                    .builder()
                    .membershipRequest(
                        MembershipRequestDTO
                            .builder()
                            .reader(
                                ReaderDTO
                                    .builder()
                                    .username("foo")
                                    .build()
                            )
                            .bookClub(
                                BookClubDTO
                                    .builder()
                                    .name("foo")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );
        });

        securityUtilMockedStatic.close();
    }

    @Test
    void testReviewMembershipRequest_ReviewerNotMember() {
        var testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(
            Reader
                .builder()
                .id(testID)
                .username("foo")
                .build()
        );
        when(membershipRequestRepo.findById(any(UUID.class))).thenReturn(Optional.of(
            MembershipRequest
                .builder()
                .id(UUID.randomUUID())
                .reader(
                    Reader
                        .builder()
                        .id(UUID.randomUUID())
                        .build()
                )
                .bookClub(
                    BookClub
                        .builder()
                        .members(Set.of(
                            BookClubMembership
                                .builder()
                                .clubRole(BookClubRole.ADMIN)
                                .reader(
                                    Reader
                                        .builder()
                                        .id(UUID.randomUUID())
                                        .build()
                                )
                                .build()
                        ))
                        .build()
                )
                .status(RequestStatus.OPEN)
                .build()
        ));

        assertThrows(ReaderNotFoundException.class, () -> membershipRequestService.reviewMembershipRequest(
            MembershipRequestAction
                .builder()
                .role(BookClubRole.READER)
                .reviewMessage("bar")
                .action(RequestAction.APPROVE)
                .membershipRequest(
                    MembershipRequestDTO
                        .builder()
                        .id(UUID.randomUUID())
                        .reader(
                            ReaderDTO
                                .builder()
                                .id(UUID.randomUUID())
                                .username("foo")
                                .build()
                        )
                        .bookClub(
                            BookClubDTO
                                .builder()
                                .name("foo")
                                .build()
                        )
                        .build()
                )
                .build()
        ));

        verify(membershipRequestRepo, times(1)).findById(any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));
        verify(membershipRequestRepo, times(0)).save(any(MembershipRequest.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testReviewMembershipRequest_ReviewerNotAdmin() {
        var testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(
            Reader
                .builder()
                .id(testID)
                .username("foo")
                .build()
        );
        when(membershipRequestRepo.findById(any(UUID.class))).thenReturn(Optional.of(
            MembershipRequest
                .builder()
                .id(UUID.randomUUID())
                .reader(
                    Reader
                        .builder()
                        .id(UUID.randomUUID())
                        .build()
                )
                .bookClub(
                    BookClub
                        .builder()
                        .members(Set.of(
                            BookClubMembership
                                .builder()
                                .clubRole(BookClubRole.READER)
                                .reader(
                                    Reader
                                        .builder()
                                        .id(testID)
                                        .build()
                                )
                                .build()
                        ))
                        .build()
                )
                .status(RequestStatus.OPEN)
                .build()
        ));

        assertThrows(UnauthorizedBookClubActionException.class, () -> membershipRequestService.reviewMembershipRequest(
            MembershipRequestAction
                .builder()
                .role(BookClubRole.READER)
                .reviewMessage("bar")
                .action(RequestAction.APPROVE)
                .membershipRequest(
                    MembershipRequestDTO
                        .builder()
                        .id(UUID.randomUUID())
                        .reader(
                            ReaderDTO
                                .builder()
                                .id(UUID.randomUUID())
                                .username("foo")
                                .build()
                        )
                        .bookClub(
                            BookClubDTO
                                .builder()
                                .name("foo")
                                .build()
                        )
                        .build()
                )
                .build()
        ));

        verify(membershipRequestRepo, times(1)).findById(any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));
        verify(membershipRequestRepo, times(0)).save(any(MembershipRequest.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testReviewMembershipRequest_AlreadyReviewed() {
        var testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(
            Reader
                .builder()
                .id(testID)
                .username("foo")
                .build()
        );
        when(membershipRequestRepo.findById(any(UUID.class))).thenReturn(Optional.of(
            MembershipRequest
                .builder()
                .id(UUID.randomUUID())
                .reader(
                    Reader
                        .builder()
                        .id(UUID.randomUUID())
                        .build()
                )
                .bookClub(
                    BookClub
                        .builder()
                        .members(Set.of(
                            BookClubMembership
                                .builder()
                                .clubRole(BookClubRole.ADMIN)
                                .reader(
                                    Reader
                                        .builder()
                                        .id(testID)
                                        .build()
                                )
                                .build()
                        ))
                        .build()
                )
                .status(RequestStatus.APPROVED)
                .build()
        ));

        assertThrows(BadBookClubActionException.class, () -> membershipRequestService.reviewMembershipRequest(
            MembershipRequestAction
                .builder()
                .role(BookClubRole.READER)
                .reviewMessage("bar")
                .action(RequestAction.APPROVE)
                .membershipRequest(
                    MembershipRequestDTO
                        .builder()
                        .id(UUID.randomUUID())
                        .reader(
                            ReaderDTO
                                .builder()
                                .id(UUID.randomUUID())
                                .username("foo")
                                .build()
                        )
                        .bookClub(
                            BookClubDTO
                                .builder()
                                .name("foo")
                                .build()
                        )
                        .build()
                )
                .build()
        ));

        verify(membershipRequestRepo, times(1)).findById(any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));
        verify(membershipRequestRepo, times(0)).save(any(MembershipRequest.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testReviewMembershipRequest_ApproveExistingMember() {
        var testReviewerID = UUID.randomUUID();
        var testMemberID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(
            Reader
                .builder()
                .id(testReviewerID)
                .username("foo")
                .build()
        );
        when(membershipRequestRepo.findById(any(UUID.class))).thenReturn(Optional.of(
            MembershipRequest
                .builder()
                .id(UUID.randomUUID())
                .reader(
                    Reader
                        .builder()
                        .id(testMemberID)
                        .build()
                )
                .bookClub(
                    BookClub
                        .builder()
                        .members(Set.of(
                            BookClubMembership
                                .builder()
                                .clubRole(BookClubRole.ADMIN)
                                .reader(
                                    Reader
                                        .builder()
                                        .id(testReviewerID)
                                        .build()
                                )
                                .build(),
                            BookClubMembership
                                .builder()
                                .clubRole(BookClubRole.READER)
                                .reader(
                                    Reader
                                        .builder()
                                        .id(testMemberID)
                                        .build()
                                )
                                .build()
                        ))
                        .build()
                )
                .status(RequestStatus.OPEN)
                .build()
        ));

        assertThrows(BadBookClubActionException.class, () -> membershipRequestService.reviewMembershipRequest(
            MembershipRequestAction
                .builder()
                .role(BookClubRole.READER)
                .reviewMessage("bar")
                .action(RequestAction.APPROVE)
                .membershipRequest(
                    MembershipRequestDTO
                        .builder()
                        .id(UUID.randomUUID())
                        .reader(
                            ReaderDTO
                                .builder()
                                .id(testMemberID)
                                .username("foo")
                                .build()
                        )
                        .bookClub(
                            BookClubDTO
                                .builder()
                                .name("foo")
                                .build()
                        )
                        .build()
                )
                .build()
        ));

        verify(membershipRequestRepo, times(1)).findById(any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));
        verify(membershipRequestRepo, times(0)).save(any(MembershipRequest.class));

        securityUtilMockedStatic.close();
    }
}
