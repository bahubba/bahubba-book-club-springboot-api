package com.bahubba.bahubbabookclub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Reader not found")
public class ReaderNotFoundException extends RuntimeException {
    public ReaderNotFoundException(String usernameOrEmail) {
        super("Reader could not be found with username or email matching '" + usernameOrEmail + "'");
    }

    public ReaderNotFoundException(UUID id) {
        super("Reader could not be found with ID '" + id + "'");
    }
}
