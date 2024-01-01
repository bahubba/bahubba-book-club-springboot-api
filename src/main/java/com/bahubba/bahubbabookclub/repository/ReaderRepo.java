package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.Reader;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA Repository for the {@link Reader} entity */
@Repository
public interface ReaderRepo extends JpaRepository<Reader, UUID> {
    Optional<Reader> findByUsername(final String username);

    Optional<Reader> findByUsernameAndDepartedIsNull(final String username);

    Optional<Reader> findByEmail(final String email);

    Optional<Reader> findByUsernameOrEmail(final String username, final String email);

    boolean existsByUsername(final String username);

    boolean existsByEmail(final String email);

    boolean existsByUsernameOrEmail(final String username, final String email);
}
