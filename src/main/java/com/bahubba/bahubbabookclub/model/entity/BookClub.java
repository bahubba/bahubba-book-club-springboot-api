package com.bahubba.bahubbabookclub.model.entity;

import com.bahubba.bahubbabookclub.model.enums.Publicity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "book_club")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookClub implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @NotNull
    private UUID id;

    @Column(nullable = false, unique = true)
    @NotNull
    private String name;

    @Column(name = "image_url")
    private String imageURL;

    @Column(nullable = false)
    @NotNull
    @Builder.Default
    private String description = "A book club for reading books!";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    private Publicity publicity = Publicity.PRIVATE;

    @OneToMany(mappedBy = "bookClub", fetch = FetchType.LAZY)
    private Set<BookClubMembership> members;

    @Column(nullable = false)
    @NotNull
    @Builder.Default
    private LocalDateTime created = LocalDateTime.now();

    @Column
    private LocalDateTime disbanded;
}
