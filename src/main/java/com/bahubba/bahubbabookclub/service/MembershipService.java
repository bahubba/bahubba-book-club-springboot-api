package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.OwnershipChange;

import java.util.List;
import java.util.UUID;

public interface MembershipService {
    List<BookClubMembershipDTO> getAll(String bookClubName);

    BookClubRole getRole(String bookClubName);

    BookClubMembershipDTO getMembership(String bookClubName);

    BookClubMembershipDTO updateMembership(MembershipUpdate membershipUpdate);

    BookClubMembershipDTO deleteMembership(String bookClubName, UUID readerID);

    Boolean changeOwnership(OwnershipChange ownershipChange);
}
