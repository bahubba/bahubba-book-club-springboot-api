package com.bahubba.bahubbabookclub.model.dto;

import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookClubMembershipDTO {
    private BookClubDTO bookClub;
    private ReaderDTO reader;
    private BookClubRole clubRole;
    private boolean isOwner;
    private LocalDateTime joined;
    private LocalDateTime departed;
}
