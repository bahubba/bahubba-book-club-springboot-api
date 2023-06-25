package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.config.JwtService;
import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.exception.TokenRefreshException;
import com.bahubba.bahubbabookclub.model.dto.MessageResponseDTO;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.entity.RefreshToken;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import com.bahubba.bahubbabookclub.repository.RefreshTokenRepo;
import com.bahubba.bahubbabookclub.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

// TODO - This should probably be merged with JwtService; There's too much overlap
@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Autowired
    private ReaderRepo readerRepo;

    @Autowired
    JwtService jwtService;

    @Override
    public String getJwtRefreshFromCookies(HttpServletRequest req) {
        return jwtService.getJwtRefreshFromCookies(req);
    }

    @Override
    public ResponseEntity<MessageResponseDTO> refreshToken(String refreshToken) {
        return getByToken(refreshToken)
            .map(this::verifyExpiration)
            .map(RefreshToken::getReader)
            .map(reader -> {
                ResponseCookie jwtCookie = jwtService.generateJwtCookie(reader);

                return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(MessageResponseDTO.builder().message("Token refreshed").build());
            })
            .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token doesn't exist"));
    }

    @Override
    public Optional<RefreshToken> getByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    @Override
    public RefreshToken createRefreshToken(UUID readerID) {
        // Get the current reader
        Reader reader = readerRepo.findById(readerID).orElseThrow(() -> new ReaderNotFoundException(readerID));

        RefreshToken refreshToken = RefreshToken
            .builder()
            .reader(reader)
            .token(UUID.randomUUID().toString())
            .expiryDate(Instant.now().plusMillis(1000L * 60L * 60L))
            .build();

        return refreshTokenRepo.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepo.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token expired");
        }

        return token;
    }

    @Override
    public int deleteByReaderID(UUID readerID) {
        return refreshTokenRepo.deleteByReader(
            readerRepo.findById(readerID)
                .orElseThrow(() -> new ReaderNotFoundException(readerID))
        );
    }
}
