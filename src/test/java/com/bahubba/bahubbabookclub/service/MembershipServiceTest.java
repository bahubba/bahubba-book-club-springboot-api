package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.OwnershipChange;
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link MembershipService} business logic
 */
@SpringBootTest
@ActiveProfiles("test")
class MembershipServiceTest {
    @Autowired
    private MembershipService membershipService;

    @MockBean
    BookClubRepo bookClubRepo;

    @MockBean
    BookClubMembershipRepo bookClubMembershipRepo;

    @Test
    void testGetAll() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(testID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndClubRoleAndReaderId(anyString(), any(BookClubRole.class), any(UUID.class))).thenReturn(Optional.of(
            BookClubMembership
                .builder()
                .reader(Reader.builder().id(testID).build())
                .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                .clubRole(BookClubRole.ADMIN)
                .build()
        ));
        when(bookClubMembershipRepo.findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class)))
            .thenReturn(Page.empty());

        Page<BookClubMembershipDTO> result = membershipService.getAll("foo", 1, 1);

        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndClubRoleAndReaderId(anyString(), any(BookClubRole.class), any(UUID.class));
        verify(bookClubMembershipRepo, times(1))
            .findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetAll_ReaderNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(ReaderNotFoundException.class, () -> membershipService.getAll("foo", 1, 1));

        verify(bookClubMembershipRepo, times(0))
            .findByBookClubNameAndClubRoleAndReaderId(anyString(), any(BookClubRole.class), any(UUID.class));
        verify(bookClubMembershipRepo, times(0))
            .findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetAll_ReaderNotMemberOrNotAdmin() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(UUID.randomUUID()).build());

        when(bookClubMembershipRepo.findByBookClubNameAndClubRoleAndReaderId(
            anyString(), any(BookClubRole.class), any(UUID.class))
        ).thenReturn(Optional.empty());

        assertThrows(UnauthorizedBookClubActionException.class, () -> membershipService.getAll("foo", 1, 1));
        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndClubRoleAndReaderId(anyString(), any(BookClubRole.class), any(UUID.class));
        verify(bookClubMembershipRepo, times(0))
            .findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetAll_NegativePageSize() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(testID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndClubRoleAndReaderId(anyString(), any(BookClubRole.class), any(UUID.class))).thenReturn(Optional.of(
            BookClubMembership
                .builder()
                .reader(Reader.builder().id(testID).build())
                .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                .clubRole(BookClubRole.ADMIN)
                .build()
        ));
        when(bookClubMembershipRepo.findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class)))
            .thenReturn(Page.empty());

        assertThrows(PageSizeTooSmallException.class, () -> membershipService.getAll("foo", 1, -1));
        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndClubRoleAndReaderId(anyString(), any(BookClubRole.class), any(UUID.class));
        verify(bookClubMembershipRepo, times(1))
            .findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetAll_TooLargePageSize() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(testID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndClubRoleAndReaderId(anyString(), any(BookClubRole.class), any(UUID.class))).thenReturn(Optional.of(
            BookClubMembership
                .builder()
                .reader(Reader.builder().id(testID).build())
                .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                .clubRole(BookClubRole.ADMIN)
                .build()
        ));
        when(bookClubMembershipRepo.findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class)))
            .thenReturn(Page.empty());

        assertThrows(PageSizeTooLargeException.class, () -> membershipService.getAll("foo", 1, 51));
        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndClubRoleAndReaderId(anyString(), any(BookClubRole.class), any(UUID.class));
        verify(bookClubMembershipRepo, times(1))
            .findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetRole() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());
        when(bookClubMembershipRepo.findByBookClubNameAndReaderId(anyString(), any(UUID.class))).thenReturn(Optional.of(new BookClubMembership()));

        BookClubRole result = membershipService.getRole("foo");

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndReaderId(anyString(), any(UUID.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testGetRole_NoReader() {
        assertThrows(ReaderNotFoundException.class, () -> membershipService.getRole("foo"));
    }

    @Test
    void testGetMembership() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderId(anyString(), any(UUID.class))).thenReturn(Optional.of(new BookClubMembership()));
        BookClubMembershipDTO result = membershipService.getMembership("foo");

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndReaderId(anyString(), any(UUID.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetMembership_NoReader() {
        assertThrows(ReaderNotFoundException.class, () -> membershipService.getMembership("foo"));
    }

    @Test
    void testGetMembership_NoMembership() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(Reader.builder().id(UUID.randomUUID()).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderId(anyString(), any(UUID.class))).thenReturn(Optional.empty());
        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.of(BookClub.builder().build()));
        BookClubMembershipDTO result = membershipService.getMembership("foo");

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndReaderId(anyString(), any(UUID.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership() {
        UUID testReaderID = UUID.randomUUID();
        UUID testUpdateReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
            anyString(), any(UUID.class), any(BookClubRole.class))
        ).thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class)))
            .thenReturn(Optional.of(
                BookClubMembership
                    .builder()
                    .bookClub(
                        BookClub
                            .builder()
                            .id(UUID.randomUUID())
                            .build()
                    )
                    .reader(
                        Reader
                            .builder()
                            .id(testUpdateReaderID)
                            .build()
                    )
                    .isCreator(false)
                    .build()
            ));
        when(bookClubMembershipRepo.save(any(BookClubMembership.class))).thenReturn(new BookClubMembership());

        BookClubMembershipDTO result = membershipService.updateMembership(
            MembershipUpdate
                .builder()
                .bookClubName("foo")
                .readerID(testUpdateReaderID)
                .role(BookClubRole.ADMIN)
                .build()
        );

        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).save(any(BookClubMembership.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_NoReader() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(ReaderNotFoundException.class, () -> membershipService.updateMembership(MembershipUpdate.builder().build()));
        verify(bookClubMembershipRepo, times(0)).findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(0)).findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_UpdatingSelf() {
        UUID testReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        assertThrows(BadBookClubActionException.class, () -> membershipService.updateMembership(
            MembershipUpdate
                .builder()
                .bookClubName("foo")
                .readerID(testReaderID)
                .role(BookClubRole.ADMIN)
                .build()
        ));

        verify(bookClubMembershipRepo, times(0))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(0))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_RequesterNotMember() {
        UUID testReaderID = UUID.randomUUID();
        UUID testUpdateReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
            anyString(), any(UUID.class), any(BookClubRole.class))
        ).thenReturn(Optional.empty());

        assertThrows(UnauthorizedBookClubActionException.class, () -> membershipService.updateMembership(
            MembershipUpdate
                .builder()
                .bookClubName("foo")
                .readerID(testUpdateReaderID)
                .role(BookClubRole.ADMIN)
                .build()
        ));

        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(0))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_TargetReaderNotMember() {
        UUID testReaderID = UUID.randomUUID();
        UUID testUpdateReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
            anyString(), any(UUID.class), any(BookClubRole.class))
        ).thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class)))
            .thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class, () -> membershipService.updateMembership(
            MembershipUpdate
                .builder()
                .bookClubName("foo")
                .readerID(testUpdateReaderID)
                .role(BookClubRole.ADMIN)
                .build()
        ));

        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_UpdateCreator() {
        UUID testReaderID = UUID.randomUUID();
        UUID testUpdateReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
            anyString(), any(UUID.class), any(BookClubRole.class))
        ).thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class)))
            .thenReturn(Optional.of(
                BookClubMembership
                    .builder()
                    .bookClub(
                        BookClub
                            .builder()
                            .id(UUID.randomUUID())
                            .build()
                    )
                    .reader(
                        Reader
                            .builder()
                            .id(testUpdateReaderID)
                            .build()
                    )
                    .isCreator(true)
                    .build()
            ));

        assertThrows(BadBookClubActionException.class, () -> membershipService.updateMembership(
            MembershipUpdate
                .builder()
                .bookClubName("foo")
                .readerID(testUpdateReaderID)
                .role(BookClubRole.ADMIN)
                .build()
        ));

        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_NoUpdate() {
        UUID testReaderID = UUID.randomUUID();
        UUID testUpdateReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
            anyString(), any(UUID.class), any(BookClubRole.class))
        ).thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class)))
            .thenReturn(Optional.of(
                BookClubMembership
                    .builder()
                    .bookClub(
                        BookClub
                            .builder()
                            .id(UUID.randomUUID())
                            .build()
                    )
                    .reader(
                        Reader
                            .builder()
                            .id(testUpdateReaderID)
                            .build()
                    )
                    .clubRole(BookClubRole.ADMIN)
                    .isCreator(false)
                    .build()
            ));
        when(bookClubMembershipRepo.save(any(BookClubMembership.class))).thenReturn(new BookClubMembership());

        assertThrows(BadBookClubActionException.class, () -> membershipService.updateMembership(
            MembershipUpdate
                .builder()
                .bookClubName("foo")
                .readerID(testUpdateReaderID)
                .role(BookClubRole.ADMIN)
                .build()
        ));

        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership() {
        UUID testReaderID = UUID.randomUUID();
        UUID testDeleteReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
            anyString(), any(UUID.class), any(BookClubRole.class))
        ).thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class)))
            .thenReturn(Optional.of(
                BookClubMembership
                    .builder()
                    .bookClub(
                        BookClub
                            .builder()
                            .id(UUID.randomUUID())
                            .build()
                    )
                    .reader(
                        Reader
                            .builder()
                            .id(testDeleteReaderID)
                            .build()
                    )
                    .isCreator(false)
                    .build()
            ));
        when(bookClubMembershipRepo.save(any(BookClubMembership.class))).thenReturn(new BookClubMembership());

        BookClubMembershipDTO result = membershipService.deleteMembership("foo", testDeleteReaderID);

        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).save(any(BookClubMembership.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership_NoReader() {MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(ReaderNotFoundException.class, () -> membershipService.deleteMembership("foo", UUID.randomUUID()));

        verify(bookClubMembershipRepo, times(0))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(0))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership_DeletingSelf() {
        UUID testReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        assertThrows(BadBookClubActionException.class, () -> membershipService.deleteMembership("foo", testReaderID));

        verify(bookClubMembershipRepo, times(0))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(0))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership_RequesterNotAdmin() {
        UUID testReaderID = UUID.randomUUID();
        UUID testDeleteReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
            anyString(), any(UUID.class), any(BookClubRole.class))
        ).thenReturn(Optional.empty());

        assertThrows(UnauthorizedBookClubActionException.class, () -> membershipService.deleteMembership(
            "foo", testDeleteReaderID
        ));

        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(0))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership_targetReaderNotMember() {
        UUID testReaderID = UUID.randomUUID();
        UUID testDeleteReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
            anyString(), any(UUID.class), any(BookClubRole.class))
        ).thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class)))
            .thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class, () -> membershipService.deleteMembership("foo", testDeleteReaderID));

        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership_targetReaderCreator() {
        UUID testReaderID = UUID.randomUUID();
        UUID testDeleteReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
            anyString(), any(UUID.class), any(BookClubRole.class))
        ).thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class)))
            .thenReturn(Optional.of(
                BookClubMembership
                    .builder()
                    .bookClub(
                        BookClub
                            .builder()
                            .id(UUID.randomUUID())
                            .build()
                    )
                    .reader(
                        Reader
                            .builder()
                            .id(testDeleteReaderID)
                            .build()
                    )
                    .isCreator(true)
                    .build()
            ));

        assertThrows(BadBookClubActionException.class, () -> membershipService.deleteMembership("foo", testDeleteReaderID));

        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndClubRoleAndDepartedIsNull(
                anyString(), any(UUID.class), any(BookClubRole.class)
            );
        verify(bookClubMembershipRepo, times(1))
            .findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testChangeOwnership() {
        UUID testReaderID = UUID.randomUUID();
        UUID testNewOwnerID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndIsCreatorTrue(anyString(), any(UUID.class)))
            .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class)))
            .thenReturn(Optional.of(
                BookClubMembership
                    .builder()
                    .bookClub(BookClub.builder().build())
                    .reader(
                        Reader
                            .builder()
                            .id(testNewOwnerID)
                            .build()
                    )
                    .isCreator(false)
                    .build()
            ));
        when(bookClubMembershipRepo.save(any(BookClubMembership.class))).thenReturn(new BookClubMembership());

        Boolean result = membershipService.changeOwnership(
            OwnershipChange
                .builder()
                .bookClubName("foo")
                .newOwnerID(testNewOwnerID)
                .build()
        );

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndReaderIdAndIsCreatorTrue(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).save(any(BookClubMembership.class));
        assertThat(result).isTrue();

        securityUtilMockedStatic.close();
    }

    @Test
    void testChangeOwnership_NoReader() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(null);

        assertThrows(ReaderNotFoundException.class, () -> membershipService.changeOwnership(
            OwnershipChange
                .builder()
                .bookClubName("foo")
                .newOwnerID(UUID.randomUUID())
                .build()
        ));

        verify(bookClubMembershipRepo, times(0)).findByBookClubNameAndReaderIdAndIsCreatorTrue(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testChangeOwnership_NoOwnerChange() {
        UUID testReaderID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        assertThrows(BadBookClubActionException.class, () -> membershipService.changeOwnership(
            OwnershipChange
                .builder()
                .bookClubName("foo")
                .newOwnerID(testReaderID)
                .build()
        ));

        verify(bookClubMembershipRepo, times(0)).findByBookClubNameAndReaderIdAndIsCreatorTrue(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testChangeOwnership_NotOwner() {
        UUID testReaderID = UUID.randomUUID();
        UUID testNewOwnerID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndIsCreatorTrue(anyString(), any(UUID.class)))
            .thenReturn(Optional.empty());

        assertThrows(UnauthorizedBookClubActionException.class, () -> membershipService.changeOwnership(
            OwnershipChange
                .builder()
                .bookClubName("foo")
                .newOwnerID(testNewOwnerID)
                .build()
        ));

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndReaderIdAndIsCreatorTrue(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testChangeOwnership_NewOwnerNotMember() {
        UUID testReaderID = UUID.randomUUID();
        UUID testNewOwnerID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails)
            .thenReturn(Reader.builder().id(testReaderID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndIsCreatorTrue(anyString(), any(UUID.class)))
            .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class)))
            .thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class, () -> membershipService.changeOwnership(
            OwnershipChange
                .builder()
                .bookClubName("foo")
                .newOwnerID(testNewOwnerID)
                .build()
        ));

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndReaderIdAndIsCreatorTrue(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndReaderIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }
}
