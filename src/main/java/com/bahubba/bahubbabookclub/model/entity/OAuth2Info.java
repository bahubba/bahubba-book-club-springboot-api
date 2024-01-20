package com.bahubba.bahubbabookclub.model.entity;

import com.bahubba.bahubbabookclub.model.enums.OAuth2Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "oauth2_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Info implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OAuth2Provider provider;

    @Column
    private String name;

    @Column
    private String imageURL;
}
