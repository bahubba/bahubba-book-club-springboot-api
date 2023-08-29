package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository for the {@link BookClubMembership} entity
 */
@Repository
public interface BookClubMembershipRepo extends JpaRepository<BookClubMembership, UUID> {
}
