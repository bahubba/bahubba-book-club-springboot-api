package com.bahubba.bahubbabookclub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for unauthorized book club actions
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
public class UnauthorizedBookClubActionException extends RuntimeException {
    public UnauthorizedBookClubActionException() {
        super("Unauthorized to perform action on book club");
    }
}
