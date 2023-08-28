package com.bahubba.bahubbabookclub.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data sent with HTTP request for searching for book clubs
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookClubSearch {
    private String searchTerm;
    private int page;
    private int pageSize;
}
