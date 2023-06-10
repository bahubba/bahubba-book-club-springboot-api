package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;

public interface AuthService {
    public AuthDTO register(NewReader newReader);

    public AuthDTO authenticate(AuthRequest req);
}
