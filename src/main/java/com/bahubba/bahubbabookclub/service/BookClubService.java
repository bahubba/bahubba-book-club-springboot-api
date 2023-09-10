package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;

import java.util.List;
import java.util.UUID;

public interface BookClubService {
    BookClubDTO create(NewBookClub newBookClub);

    BookClubDTO update(BookClubDTO bookClubDTO);

    BookClubDTO findByID(UUID id);

    BookClubDTO findByName(String name);

    List<BookClubDTO> findAllForReader();

    List<BookClubDTO> findAll();

    List<BookClubDTO> search(String searchTerm);

    BookClubDTO disbandBookClubByID(UUID id);

    BookClubDTO disbandBookClubByName(String name);
}
