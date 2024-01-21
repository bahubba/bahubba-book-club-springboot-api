package com.bahubba.bahubbabookclub.model.entity;

import com.bahubba.bahubbabookclub.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Users (users) */
@Entity
@Table(name = "app_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    @NotNull private String username;

    @Column(nullable = false, unique = true)
    @NotNull private String email;

    @Column(name = "given_name")
    private String givenName;

    @Column(name = "middle_name")
    private String middleName;

    @Column
    private String surname;

    @Column
    private String suffix;

    @Column
    private String title;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<BookClubMembership> memberships;

    // TODO - Preferred provider? Possibly useful for consistent profile picture
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_oauth2_info", joinColumns = @JoinColumn(name = "user_id"))
    @AttributeOverride(name = "provider", column = @Column(name = "provider"))
    @AttributeOverride(name = "name", column = @Column(name = "name"))
    @AttributeOverride(name = "imageURL", column = @Column(name = "image_url"))
    private Set<OAuth2Info> oAuth2Info;

    @Column(nullable = false)
    @NotNull @Builder.Default
    private LocalDateTime joined = LocalDateTime.now();

    @Column
    private LocalDateTime departed;

    @Column(nullable = false)
    @NotNull @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
}
