package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;

public interface MembershipRequestService {
    MembershipRequestDTO requestMembership(NewMembershipRequest newMembershipRequest);

    Boolean hasPendingRequest(String bookClubName);
}
