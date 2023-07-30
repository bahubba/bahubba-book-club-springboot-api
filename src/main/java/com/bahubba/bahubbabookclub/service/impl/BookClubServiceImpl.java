package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.BookClubNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.mapper.BookClubMapper;
import com.bahubba.bahubbabookclub.model.payload.NewBookClub;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.service.BookClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BookClubServiceImpl implements BookClubService {
    @Autowired
    private BookClubRepo bookClubRepo;

    @Autowired
    private BookClubMapper bookClubMapper;

    @Override
    public BookClubDTO create(NewBookClub newBookClub) {
        BookClub newBookClubEntity = bookClubMapper.modelToEntity(newBookClub);
        // TODO - Get the authenticated user's info and add a membership record for them in the book club
        return bookClubMapper.entityToDTO(bookClubRepo.save(newBookClubEntity));
    }

    @Override
    public BookClubDTO findByID(UUID id) {
        return bookClubMapper.entityToDTO(
            bookClubRepo.findById(id).orElseThrow(() -> new BookClubNotFoundException(id))
        );
    }
    
    @Override
    public List<BookClubDTO> findAllForReader() {

        return bookClubMapper.entityListToDTO(bookClubRepo.findAll());
    }

    @Override
    public List<BookClubDTO> findAll() {
        return bookClubMapper.entityListToDTO(bookClubRepo.findAll());
    }

    @Override
    public BookClubDTO disbandBookClub(UUID id) {
        BookClub bookClub = bookClubRepo.findById(id).orElseThrow(() -> new BookClubNotFoundException(id));
        bookClub.setDisbanded(LocalDateTime.now());
        return bookClubMapper.entityToDTO(bookClubRepo.save(bookClub));
    }
}
