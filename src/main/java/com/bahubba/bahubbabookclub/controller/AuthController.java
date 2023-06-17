package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.dto.ReaderDTO;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.service.AuthService;
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

    @PostMapping("/register")
    public ResponseEntity<ReaderDTO> register (@RequestBody NewReader newReader) {
        AuthDTO authDTO = authService.register(newReader);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
            .body(authDTO.getReader());
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ReaderDTO> authenticate (@RequestBody AuthRequest req) {
        AuthDTO authDTO = authService.authenticate(req);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
            .body(authDTO.getReader());
    }
}
