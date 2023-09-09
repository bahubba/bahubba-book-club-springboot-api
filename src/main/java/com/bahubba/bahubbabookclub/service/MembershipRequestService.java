package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.payload.MembershipRequestAction;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;

import java.util.List;

public interface MembershipRequestService {
    MembershipRequestDTO requestMembership(NewMembershipRequest newMembershipRequest);

    Boolean hasPendingRequest(String bookClubName);

    List<MembershipRequestDTO> getMembershipRequestsForBookClub(String bookClubName);

    MembershipRequestDTO reviewMembershipRequest(MembershipRequestAction membershipRequestAction);
}
