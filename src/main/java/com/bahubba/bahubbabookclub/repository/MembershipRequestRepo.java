package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * JPA Repository for the {@link MembershipRequest} entity
 */
public interface MembershipRequestRepo extends JpaRepository<MembershipRequest, UUID> {
}
