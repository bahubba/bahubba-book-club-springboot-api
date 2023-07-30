package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.entity.Notification;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.entity.RefreshToken;
import com.bahubba.bahubbabookclub.model.enums.Role;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.repository.NotificationRepo;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import com.bahubba.bahubbabookclub.repository.RefreshTokenRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthServiceTest {
    @Autowired
    AuthService authService;

    @MockBean
    ReaderRepo readerRepo;

    @MockBean
    JwtService jwtService;

    @MockBean
    AuthenticationManager authManager;

    @MockBean
    RefreshTokenRepo refreshTokenRepo;

    @MockBean
    NotificationRepo notificationRepo;

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

        when(jwtService.createRefreshToken(any(UUID.class))).thenReturn(
            RefreshToken
                .builder()
                .token("foobar")
                .build()
        );

        when(jwtService.generateJwtCookie(any(Reader.class))).thenReturn(
            ResponseCookie.from("foo", "bar").build()
        );

        AuthDTO result = authService.register(NewReader.builder().password("password").build());

        verify(notificationRepo, times(1)).save(any(Notification.class));
        verify(jwtService, times(1)).generateJwtCookie(any(Reader.class));
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

        when(readerRepo.findById(any(UUID.class))).thenReturn(
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

        when(jwtService.createRefreshToken(any(UUID.class))).thenReturn(
            RefreshToken
                .builder()
                .token("foobar")
                .build()
        );

        when(jwtService.generateJwtCookie(any(Reader.class))).thenReturn(
                ResponseCookie.from("foo", "bar").build()
        );

        AuthDTO result = authService.authenticate(
            AuthRequest.builder().usernameOrEmail("username").password("password").build()
        );

        verify(jwtService, times(1)).generateJwtCookie(any(Reader.class));
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

    @Test
    void testLogout() {
        when(jwtService.generateCookie(anyString(), anyString(), anyString())).thenReturn(
            ResponseCookie.from("foo", "").build()
        );

        AuthDTO result = authService.logout(new MockHttpServletRequest());

        verify(jwtService, times(1)).deleteRefreshToken(any(HttpServletRequest.class));
        verify(jwtService, times(2)).generateCookie(anyString(), anyString(), anyString());
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getToken().getValue()).isEmpty();
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getRefreshToken().getValue()).isEmpty();
    }
}
