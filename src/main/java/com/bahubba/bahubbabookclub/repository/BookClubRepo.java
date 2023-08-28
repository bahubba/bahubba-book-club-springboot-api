package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.enums.Publicity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookClubRepo extends JpaRepository<BookClub, UUID> {
    Optional<BookClub> findByName(final String name);

    @Query(
        nativeQuery = true,
        value = "SELECT bc.* FROM book_club bc " +
            "INNER JOIN book_club_readers bcr " +
            "ON bc.id = bcr.book_club_id " +
            "INNER JOIN reader r " +
            "ON bcr.reader_id = r.id " +
            "WHERE bc.disbanded IS NULL " +
            "AND bcr.departed IS NULL " +
            "AND r.id = :readerId"
    )
    List<BookClub> findAllForReader(final UUID readerId);

    List<BookClub> findByPublicityNotAndNameContainingIgnoreCase(final Publicity publicity, final String searchTerm);
}
