package com.bahubba.bahubbabookclub.model.payload;

import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipUpdate {
    private String bookClubName;
    private UUID readerID;
    private BookClubRole role;
}
