package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.mapper.ReaderMapper;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import com.bahubba.bahubbabookclub.service.AuthService;
import com.bahubba.bahubbabookclub.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Registration and authentication logic
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ReaderRepo readerRepo;

    private final ReaderMapper readerMapper;

    private final JwtService jwtService;

    private final AuthenticationManager authManager;

    /**
     * Registers a reader (user)
     * @param newReader New reader (user) information
     * @return persisted reader information
     */
    public AuthDTO register(NewReader newReader) {
        Reader reader = readerRepo.save(readerMapper.modelToEntity(newReader));

        ResponseCookie jwtCookie = jwtService.generateJwtCookie(reader);
        ResponseCookie refreshCookie = jwtService.generateJwtRefreshCookie(
            jwtService.createRefreshToken(reader.getId()).getToken()
        );

        return AuthDTO
            .builder()
            .reader(readerMapper.entityToDTO(reader))
            .token(jwtCookie)
            .refreshToken(refreshCookie)
            .build();
    }

    /**
     * Accepts user credentials and returns auth and refresh JWTs in HTTP-Only cookies
     * @param req user credentials (username and password)
     * @return the user's stored info and JWTs
     */
    public AuthDTO authenticate(AuthRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword()));

        Reader reader = readerRepo.findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())
            .orElseThrow(() -> new ReaderNotFoundException(req.getUsernameOrEmail()));

        ResponseCookie jwtCookie = jwtService.generateJwtCookie(reader);

        // Delete existing refresh cookies
        jwtService.deleteByReaderID(reader.getId());

        ResponseCookie refreshCookie = jwtService.generateJwtRefreshCookie(
            jwtService.createRefreshToken(reader.getId()).getToken()
        );

        return AuthDTO
            .builder()
            .reader(readerMapper.entityToDTO(reader))
            .token(jwtCookie)
            .refreshToken(refreshCookie)
            .build();
    }
}
