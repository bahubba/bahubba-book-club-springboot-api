package com.bahubba.bahubbabookclub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for requests to perform actions that are not possible (e.g. out of date/OBE)
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad book club action request")
public class BadBookClubActionException extends RuntimeException {
    public BadBookClubActionException() {
        super("Bad book club action request");
    }
}
