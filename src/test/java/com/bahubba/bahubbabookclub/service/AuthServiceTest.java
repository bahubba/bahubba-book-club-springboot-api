package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.config.JWTService;
import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.enums.Role;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AuthServiceTest {
    @Autowired
    AuthService authService;

    @MockBean
    ReaderRepo readerRepo;

    @MockBean
    JWTService jwtService;

    @MockBean
    AuthenticationManager authManager;

    @Test
    void testRegister() {
        UUID tstUUID = UUID.randomUUID();
        when(readerRepo.save(any(Reader.class))).thenReturn(
            Reader.builder()
                .id(tstUUID)
                .username("user")
                .email("foo@bar.foo")
                .givenName("Foo")
                .surname("Bar")
                .joined(LocalDateTime.now())
                .role(Role.USER)
                .password("password")
                .build()
        );

        when(jwtService.generateToken(any(Reader.class))).thenReturn("token");

        AuthDTO result = authService.register(NewReader.builder().password("password").build());

        verify(jwtService, times(1)).generateToken(any(Reader.class));
        assertEquals("token", result.getToken());
    }

    @Test
    void testAuthenticate() {
        UUID tstUUID = UUID.randomUUID();
        when(readerRepo.findByUsernameOrEmail(anyString(), anyString())).thenReturn(
            Optional.of(
                Reader.builder()
                    .id(tstUUID)
                    .username("user")
                    .email("foo@bar.foo")
                    .givenName("Foo")
                    .surname("Bar")
                    .joined(LocalDateTime.now())
                    .role(Role.USER)
                    .password("password")
                    .build()
            )
        );

        when(jwtService.generateToken(any(Reader.class))).thenReturn("token");

        AuthDTO result = authService.authenticate(
            AuthRequest.builder().usernameOrEmail("username").password("password").build()
        );

        verify(jwtService, times(1)).generateToken(any(Reader.class));
        assertEquals("token", result.getToken());
    }

    @Test
    void testAuthenticate_UserNotFound() {
        when(readerRepo.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());
        assertThrows(
            ReaderNotFoundException.class,
            () -> authService.authenticate(
                AuthRequest.builder().usernameOrEmail("username").password("password").build()
            )
        );
    }
}
