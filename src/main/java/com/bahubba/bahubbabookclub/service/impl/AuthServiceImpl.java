package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.config.JWTService;
import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.mapper.ReaderMapper;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import com.bahubba.bahubbabookclub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ReaderRepo readerRepo;

    private final ReaderMapper readerMapper;

    private final JWTService jwtService;

    private final AuthenticationManager authManager;

    public AuthDTO register(NewReader newReader) {
        Reader reader = readerMapper.modelToEntity(newReader);
        reader = readerRepo.save(reader);

        String jwtToken = jwtService.generateToken(reader);

        return AuthDTO.builder().token(jwtToken).build();
    }

    public AuthDTO authenticate(AuthRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword()));

        Reader reader = readerRepo.findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())
            .orElseThrow(() -> new ReaderNotFoundException(req.getUsernameOrEmail()));

        String jwtToken = jwtService.generateToken(reader);

        return AuthDTO.builder().token(jwtToken).build();
    }
}
