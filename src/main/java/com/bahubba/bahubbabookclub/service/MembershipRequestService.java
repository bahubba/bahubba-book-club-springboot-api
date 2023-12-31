package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.payload.MembershipRequestAction;
import com.bahubba.bahubbabookclub.model.payload.NewMembershipRequest;
import org.springframework.data.domain.Page;

public interface MembershipRequestService {
    MembershipRequestDTO requestMembership(NewMembershipRequest newMembershipRequest);

    Boolean hasPendingRequest(String bookClubName);

    Page<MembershipRequestDTO> getMembershipRequestsForBookClub(String bookClubName, int pageNum, int pageSize);

    MembershipRequestDTO reviewMembershipRequest(MembershipRequestAction membershipRequestAction);
}
