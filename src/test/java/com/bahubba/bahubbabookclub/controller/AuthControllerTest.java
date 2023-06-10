package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.NewReader;
import com.bahubba.bahubbabookclub.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthControllerTest {

    @Autowired
    AuthController authController;

    @MockBean
    AuthService authService;

    @Test
    void testRegisterUser() {
        when(authService.register(any(NewReader.class))).thenReturn(new AuthDTO());
        ResponseEntity<AuthDTO> rsp = authController.register(new NewReader());
        verify(authService, times(1)).register(any(NewReader.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testAuthenticate() {
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(new AuthDTO());
        ResponseEntity<AuthDTO> rsp = authController.authenticate(new AuthRequest());
        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }
}
