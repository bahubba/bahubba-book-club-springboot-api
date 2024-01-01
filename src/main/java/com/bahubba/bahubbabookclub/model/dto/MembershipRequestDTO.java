package com.bahubba.bahubbabookclub.model.dto;

import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.enums.RequestStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Membership request information to be returned to clients */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipRequestDTO {
    private UUID id;
    private ReaderDTO reader;
    private BookClubDTO bookClub;
    private String message;
    private RequestStatus status;
    private BookClubRole role;
    private Boolean viewed;
    private ReaderDTO reviewer;
    private String reviewMessage;
    private LocalDateTime requested;
    private LocalDateTime reviewed;
}
