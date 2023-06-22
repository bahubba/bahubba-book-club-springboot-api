package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.model.dto.MessageResponseDTO;
import com.bahubba.bahubbabookclub.model.entity.RefreshToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenService {

    String getJwtRefreshFromCookies(HttpServletRequest req);

    ResponseEntity<MessageResponseDTO> refreshToken(String token);

    Optional<RefreshToken> getByToken(String token);

    RefreshToken createRefreshToken(UUID readerID);

    RefreshToken verifyExpiration(RefreshToken token);

    int deleteByReaderID(UUID readerID);
}
