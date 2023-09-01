package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import com.bahubba.bahubbabookclub.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for the {@link MembershipRequest} entity
 */
public interface MembershipRequestRepo extends JpaRepository<MembershipRequest, UUID> {
    Boolean existsByBookClubNameAndReaderIdAndStatusIn(final String bookClubName, final UUID readerId, final List<RequestStatus> statuses);
}
