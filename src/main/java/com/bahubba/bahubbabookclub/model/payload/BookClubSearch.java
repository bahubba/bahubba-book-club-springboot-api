package com.bahubba.bahubbabookclub.model.payload;

import lombok.*;

/**
 * Data sent with HTTP request for searching for book clubs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookClubSearch extends PaginatedPayload {
    private String searchTerm;
}
