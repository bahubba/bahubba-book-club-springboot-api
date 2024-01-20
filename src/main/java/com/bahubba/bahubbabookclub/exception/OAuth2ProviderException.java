package com.bahubba.bahubbabookclub.exception;

import org.springframework.security.core.AuthenticationException;

public class OAuth2ProviderException extends AuthenticationException {
    public OAuth2ProviderException(String msg, Throwable t) {
        super(msg, t);
    }

    public OAuth2ProviderException(String msg) {
        super(msg);
    }
}
