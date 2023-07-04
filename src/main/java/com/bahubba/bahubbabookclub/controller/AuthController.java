package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.dto.MessageResponseDTO;
import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.service.AuthService;
import com.bahubba.bahubbabookclub.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthController:
 * Authentication endpoints
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final JwtService jwtService;

    /**
     * register: Register a reader (user)
     * @param newReader New reader (user) information
     * @return persisted reader information
     */
    @PostMapping("/register")
    public ResponseEntity<ReaderDTO> register (@RequestBody NewReader newReader) {
        AuthDTO authDTO = authService.register(newReader);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
            .header(HttpHeaders.SET_COOKIE, authDTO.getRefreshToken().toString())
            .body(authDTO.getReader());
    }

    /**
     * authenticate: Accept user credentials and return auth and refresh JWTs in HTTP-Only cookies
     * @param req user credentials (username and password)
     * @return the user's stored info
     */
    @PostMapping("/authenticate")
    public ResponseEntity<ReaderDTO> authenticate (@RequestBody AuthRequest req) {
        AuthDTO authDTO = authService.authenticate(req);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
            .header(HttpHeaders.SET_COOKIE, authDTO.getRefreshToken().toString())
            .body(authDTO.getReader());
    }

    /**
     * refreshToken: Generate a new auth (and refresh) token based on a valid refresh token
     * @param req HTTP request from the client
     * @return a string message with success status of the re-authentication
     */
    @PostMapping("/refresh")
    public ResponseEntity<MessageResponseDTO> refreshToken(HttpServletRequest req) {
        String refreshToken = jwtService.getJwtRefreshFromCookies(req);

        if (refreshToken != null && refreshToken.length() > 0) {
            return jwtService.refreshToken(refreshToken);
        }

        return ResponseEntity.badRequest().body(MessageResponseDTO.builder().message("Refresh token empty").build());
    }
}
