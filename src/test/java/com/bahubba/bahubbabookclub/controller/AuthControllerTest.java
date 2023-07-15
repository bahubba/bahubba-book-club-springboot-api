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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the authentication endpoints
 */
@SpringBootTest
class AuthControllerTest {

    @Autowired
    AuthController authController;

    @MockBean
    AuthService authService;

    @MockBean
    JwtService jwtService;

    @Test
    void testRegisterUser() {
        when(authService.register(any(NewReader.class))).thenReturn(
            AuthDTO.builder()
                .reader(new ReaderDTO())
                .token(ResponseCookie.from("foo", "bar").build())
                .refreshToken(ResponseCookie.from("bar", "foo").build())
                .build()
        );

        ResponseEntity<ResponseWrapperDTO<ReaderDTO>> rsp = authController.register(new NewReader());

        verify(authService, times(1)).register(any(NewReader.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rsp.getBody()).isNotNull();
        assertThat(rsp.getBody().getData()).isNotNull();
        assertThat(rsp.getBody().getData()).isNotNull();
    }

    @Test
    void testRegister_duplicateUsernameOrEmail() {
        when(authService.register(any(NewReader.class))).thenThrow(new DataIntegrityViolationException("some error"));

        ResponseEntity<ResponseWrapperDTO<ReaderDTO>> rsp = authController.register(new NewReader());

        verify(authService, times(1)).register(any(NewReader.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(rsp.getBody()).isNotNull();
        assertThat(rsp.getBody().getMessage()).isNotNull();
        assertThat(rsp.getBody().getData()).isNull();
    }

    @Test
    void testAuthenticate() {
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(
            AuthDTO.builder()
                .reader(new ReaderDTO())
                .token(ResponseCookie.from("foo", "bar").build())
                .refreshToken(ResponseCookie.from("bar", "foo").build())
                .build()
        );

        ResponseEntity<ResponseWrapperDTO<ReaderDTO>> rsp = authController.authenticate(new AuthRequest());

        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rsp.getBody()).isNotNull();
        assertThat(rsp.getBody().getMessage()).isNotNull();
        assertThat(rsp.getBody().getData()).isNotNull();
    }

    @Test
    void testAuthenticate_invalidCredentials() {
        when(authService.authenticate(any(AuthRequest.class))).thenThrow(new BadCredentialsException("some error"));

        ResponseEntity<ResponseWrapperDTO<ReaderDTO>> rsp = authController.authenticate(new AuthRequest());

        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(rsp.getBody()).isNotNull();
        assertThat(rsp.getBody().getMessage()).isNotNull();
        assertThat(rsp.getBody().getData()).isNull();
    }

    @Test
    void testRefreshToken() {
        when(jwtService.getJwtRefreshFromCookies(any(HttpServletRequest.class))).thenReturn("sometoken");
        when(jwtService.refreshToken(anyString())).thenReturn(
            ResponseEntity.ok().body(MessageResponseDTO.builder().message("ok").build())
        );

        ResponseEntity<MessageResponseDTO> rsp = authController.refreshToken(new MockHttpServletRequest());

        verify(jwtService, times(1)).getJwtRefreshFromCookies(any(HttpServletRequest.class));
        verify(jwtService, times(1)).refreshToken(anyString());
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testRefreshToken_nullRefreshToken() {
        when(jwtService.getJwtRefreshFromCookies(any(HttpServletRequest.class))).thenReturn(null);

        ResponseEntity<MessageResponseDTO> rsp = authController.refreshToken(new MockHttpServletRequest());

        verify(jwtService, times(1)).getJwtRefreshFromCookies(any(HttpServletRequest.class));
        verify(jwtService, times(0)).refreshToken(anyString());
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rsp.getBody()).isNotNull();
        assertThat("Refresh token empty").isEqualTo(rsp.getBody().getMessage());
    }

    @Test
    void testRefreshToken_emptyRefreshToken() {
        when(jwtService.getJwtRefreshFromCookies(any(HttpServletRequest.class))).thenReturn("");

        ResponseEntity<MessageResponseDTO> rsp = authController.refreshToken(new MockHttpServletRequest());

        verify(jwtService, times(1)).getJwtRefreshFromCookies(any(HttpServletRequest.class));
        verify(jwtService, times(0)).refreshToken(anyString());
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rsp.getBody()).isNotNull();
        assertThat("Refresh token empty").isEqualTo(rsp.getBody().getMessage());
    }
}
