package com.bahubba.bahubbabookclub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Reader not found")
public class BookClubNotFoundException extends RuntimeException {
    public BookClubNotFoundException(String name) {
        super("Book Club could not be found with name '" + name + "'");
    }

    public BookClubNotFoundException(UUID id) {
        super("Book Club could not be found with ID '" + id + "'");
    }
}
