package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.dto.MessageResponseDTO;
import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.entity.RefreshToken;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.service.AuthService;
import com.bahubba.bahubbabookclub.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<ReaderDTO> register (@RequestBody NewReader newReader) {
        AuthDTO authDTO = authService.register(newReader);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
            .header(HttpHeaders.SET_COOKIE, authDTO.getRefreshToken().toString())
            .header(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
            .body(authDTO.getReader());
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ReaderDTO> authenticate (@RequestBody AuthRequest req) {
        AuthDTO authDTO = authService.authenticate(req);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
            .header(HttpHeaders.SET_COOKIE, authDTO.getRefreshToken().toString())
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
            .body(authDTO.getReader());
    }

    @PostMapping("/refresh")
    public ResponseEntity<MessageResponseDTO> refreshToken(HttpServletRequest req) {
        String refreshToken = refreshTokenService.getJwtRefreshFromCookies(req);

        if (refreshToken != null && refreshToken.length() > 0) {
            return refreshTokenService.refreshToken(refreshToken);
        }

        return ResponseEntity.badRequest().body(MessageResponseDTO.builder().message("Refresh token empty").build());
    }
}
