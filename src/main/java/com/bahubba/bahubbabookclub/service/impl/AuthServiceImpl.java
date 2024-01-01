package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.entity.Notification;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.enums.NotificationType;
import com.bahubba.bahubbabookclub.model.mapper.ReaderMapper;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.repository.NotificationRepo;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import com.bahubba.bahubbabookclub.service.AuthService;
import com.bahubba.bahubbabookclub.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/** Registration and authentication logic */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${app.properties.auth_cookie_name}")
    private String authCookieName;

    @Value("${app.properties.refresh_cookie_name}")
    private String refreshCookieName;

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final ReaderRepo readerRepo;
    private final NotificationRepo notificationRepo;
    private final ReaderMapper readerMapper;

    @Override
    public AuthDTO register(NewReader newReader) throws ReaderNotFoundException {
        // Generate and persist a Reader entity
        Reader reader = readerRepo.save(readerMapper.modelToEntity(newReader));

        // Generate and persist a notification
        notificationRepo.save(Notification.builder()
                .sourceReader(reader)
                .targetReader(reader)
                .type(NotificationType.NEW_READER)
                .build());

        // Generate auth and refresh JWTs
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(reader);
        ResponseCookie refreshCookie = jwtService.generateJwtRefreshCookie(
                jwtService.createRefreshToken(reader.getId()).getToken());

        // Return the reader's stored info and JWTs
        return AuthDTO.builder()
                .reader(readerMapper.entityToDTO(reader))
                .token(jwtCookie)
                .refreshToken(refreshCookie)
                .build();
    }

    @Override
    public AuthDTO authenticate(@NotNull AuthRequest req) throws AuthenticationException, ReaderNotFoundException {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword()));

        Reader reader = readerRepo
                .findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())
                .orElseThrow(() -> new ReaderNotFoundException(req.getUsernameOrEmail()));

        ResponseCookie jwtCookie = jwtService.generateJwtCookie(reader);

        // Delete existing refresh cookies
        jwtService.deleteByReaderID(reader.getId());

        ResponseCookie refreshCookie = jwtService.generateJwtRefreshCookie(
                jwtService.createRefreshToken(reader.getId()).getToken());

        return AuthDTO.builder()
                .reader(readerMapper.entityToDTO(reader))
                .token(jwtCookie)
                .refreshToken(refreshCookie)
                .build();
    }

    @Override
    public AuthDTO logout(HttpServletRequest req) {
        jwtService.deleteRefreshToken(req);

        return AuthDTO.builder()
                .token(jwtService.generateCookie(authCookieName, "", ""))
                .refreshToken(jwtService.generateCookie(refreshCookieName, "", ""))
                .build();
    }
}
