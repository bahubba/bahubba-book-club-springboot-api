package com.bahubba.bahubbabookclub.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data sent with HTTP request for changing ownership of a book club
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnershipChange {
    private String bookClubName;
    private UUID newOwnerID;
}
