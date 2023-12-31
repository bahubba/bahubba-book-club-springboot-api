package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface BookClubService {
    BookClubDTO create(NewBookClub newBookClub);

    BookClubDTO update(BookClubDTO bookClubDTO);

    BookClubDTO findByID(UUID id);

    BookClubDTO findByName(String name);

    Page<BookClubDTO> findAllForReader(int pageNum, int pageSize);

    Page<BookClubDTO> findAll(int pageNum, int pageSize);

    Page<BookClubDTO> search(String searchTerm, int pageNum, int pageSize);

    BookClubDTO disbandBookClubByID(UUID id);

    BookClubDTO disbandBookClubByName(String name);
}
