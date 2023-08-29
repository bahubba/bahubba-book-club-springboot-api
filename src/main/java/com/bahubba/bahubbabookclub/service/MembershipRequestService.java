package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;

import java.util.UUID;

public interface MembershipRequestService {
    MembershipRequestDTO requestMembership(NewMembershipRequest newMembershipRequest);
}
