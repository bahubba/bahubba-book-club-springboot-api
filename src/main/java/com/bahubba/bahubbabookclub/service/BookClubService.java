package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;

import java.util.List;
import java.util.UUID;

public interface BookClubService {
    public BookClubDTO create(NewBookClub newBookClub);

    public BookClubDTO findByID(UUID id);

    public List<BookClubDTO> findAll();

    public BookClubDTO disbandBookClub(UUID id);
}
