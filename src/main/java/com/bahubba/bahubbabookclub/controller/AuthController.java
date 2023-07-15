package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.dto.MessageResponseDTO;
import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.dto.ResponseWrapperDTO;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.service.AuthService;
import com.bahubba.bahubbabookclub.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final JwtService jwtService;

    /**
     * Registers a reader (user)
     * @param newReader New reader (user) information
     * @return persisted reader information
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseWrapperDTO<ReaderDTO>> register (@RequestBody NewReader newReader) {
        try {
            AuthDTO authDTO = authService.register(newReader);

            // On success, return the user's info and JWTs in HTTP-Only cookies
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
                .header(HttpHeaders.SET_COOKIE, authDTO.getRefreshToken().toString())
                .body(
                    ResponseWrapperDTO
                        .<ReaderDTO>builder()
                        .message("User registered successfully")
                        .data(authDTO.getReader())
                        .build()
                );
        } catch(DataIntegrityViolationException e) {
            // On failure, return a message indicating the username or email already exists
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseWrapperDTO
                    .<ReaderDTO>builder()
                    .message("Username or email already exists")
                    .build()
            );
        }
    }

    /**
     * Accepts user credentials and returns auth and refresh JWTs in HTTP-Only cookies
     * @param req user credentials (username and password)
     * @return the user's stored info and JWTs
     */
    @PostMapping("/authenticate")
    public ResponseEntity<ResponseWrapperDTO<ReaderDTO>> authenticate (@RequestBody AuthRequest req) {
        try {
            AuthDTO authDTO = authService.authenticate(req);

            // On success, return the user's info and JWTs in HTTP-Only cookies
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
                .header(HttpHeaders.SET_COOKIE, authDTO.getRefreshToken().toString())
                .body(
                    ResponseWrapperDTO
                        .<ReaderDTO>builder()
                        .message("User authenticated successfully")
                        .data(authDTO.getReader())
                        .build()
                );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ResponseWrapperDTO
                    .<ReaderDTO>builder()
                    .message("Invalid credentials")
                    .build()
            );
        }
    }

    /**
     * Generates a new auth (and refresh) token based on a valid refresh token
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
