package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import com.bahubba.bahubbabookclub.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for the {@link MembershipRequest} entity
 */
public interface MembershipRequestRepo extends JpaRepository<MembershipRequest, UUID> {
    Boolean existsByBookClubNameAndReaderIdAndStatus(final String bookClubName, final UUID readerId, final RequestStatus status);

    Boolean existsByBookClubNameAndReaderIdAndStatusIn(final String bookClubName, final UUID readerId, final List<RequestStatus> statuses);

    List<MembershipRequest> findALlByBookClubIdOrderByRequestedDesc(final UUID bookClubId);

    @Modifying
    @Query("UPDATE MembershipRequest mr SET mr.status = :status WHERE mr.id = :id")
    Integer updateMembershipRequest(final UUID id, final RequestStatus status);
}
