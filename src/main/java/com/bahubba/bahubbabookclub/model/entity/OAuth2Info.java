package com.bahubba.bahubbabookclub.model.entity;

import com.bahubba.bahubbabookclub.model.enums.OAuth2Provider;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Info implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    private OAuth2Provider provider;

    private String name;

    private String imageURL;
}
