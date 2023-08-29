package com.bahubba.bahubbabookclub.model.entity;

import com.bahubba.bahubbabookclub.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Requests from Readers (users) for book club membership
 */
@Entity
@Table(name = "membership_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "reader_id", referencedColumnName = "id")
    private Reader reader;

    @ManyToOne
    @JoinColumn(name = "book_club_id", referencedColumnName = "id")
    private BookClub bookClub;

    @Column
    private String message;

    @Column
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.OPEN;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", referencedColumnName = "id")
    private Reader reviewer;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime requested = LocalDateTime.now();

    @Column
    private LocalDateTime reviewed;
}
