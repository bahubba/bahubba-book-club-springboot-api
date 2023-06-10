package com.bahubba.bahubbabookclub.model.dto;

import com.bahubba.bahubbabookclub.model.enums.Publicity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookClubDTO {
    private UUID id;
    private String name;
    private String imageURL;
    private String description;
    private Publicity publicity;
    private LocalDateTime created;
    private LocalDateTime disbanded;
}
