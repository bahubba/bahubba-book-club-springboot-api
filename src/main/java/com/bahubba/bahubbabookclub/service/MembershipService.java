package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.OwnershipChange;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MembershipService {
    Page<BookClubMembershipDTO> getAll(String bookClubName, int pageNum, int pageSize);

    BookClubRole getRole(String bookClubName);

    BookClubMembershipDTO getMembership(String bookClubName);

    BookClubMembershipDTO updateMembership(MembershipUpdate membershipUpdate);

    BookClubMembershipDTO deleteMembership(String bookClubName, UUID readerID);

    Boolean changeOwnership(OwnershipChange ownershipChange);
}
