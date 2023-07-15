package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.exception.TokenRefreshException;
import com.bahubba.bahubbabookclub.model.dto.MessageResponseDTO;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.entity.RefreshToken;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import com.bahubba.bahubbabookclub.repository.RefreshTokenRepo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the JwtService
 */
@SpringBootTest
public class JwtServiceTest {

    @Autowired
    JwtService jwtService;

    @MockBean
    private RefreshTokenRepo refreshTokenRepo;

    @MockBean
    private ReaderRepo readerRepo;

    @Value("${app.properties.auth_cookie_name}")
    private String authCookieName;

    @Value("${app.properties.refresh_cookie_name}")
    private String refreshCookieName;

    @Value("${app.properties.secret_key}")
    private String secretKey;

    @Test
    void testGenerateJwtCookie() {
        ResponseCookie result = jwtService.generateJwtCookie(Reader.builder().username("name").build());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(authCookieName);
        assertThat(result.toString().length()).isGreaterThan(0);
        assertThat(result.getPath()).isEqualTo("/api");
        assertThat(result.getMaxAge().getSeconds()).isEqualTo(24L * 60L * 60L);
        assertThat(result.isHttpOnly()).isTrue();
        assertThat(result.isSecure()).isTrue();
        assertThat(result.getDomain()).isNull();
        assertThat(result.getSameSite()).isEqualTo("None");
    }

    @Test
    void testGenerateJwtRefreshCookie() {
        ResponseCookie result = jwtService.generateJwtRefreshCookie("somecookie");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(refreshCookieName);
        assertThat(result.toString().length()).isGreaterThan(0);
        assertThat(result.getPath()).isEqualTo("/api/v1/auth/refresh");
        assertThat(result.getMaxAge().getSeconds()).isEqualTo(24L * 60L * 60L);
        assertThat(result.isHttpOnly()).isTrue();
        assertThat(result.isSecure()).isTrue();
        assertThat(result.getDomain()).isNull();
        assertThat(result.getSameSite()).isEqualTo("None");
    }

    @Test
    void testGetJwtFromCookies() {
        MockHttpServletRequest mockReq = new MockHttpServletRequest();
        mockReq.setCookies(new Cookie(authCookieName, "foo"));

        String result = jwtService.getJwtFromCookies(mockReq);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("foo");
    }

    @Test
    void testGetJwtRefreshFromCookies() {
        MockHttpServletRequest mockReq = new MockHttpServletRequest();
        mockReq.setCookies(new Cookie(refreshCookieName, "foo"));

        String result = jwtService.getJwtRefreshFromCookies(mockReq);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("foo");
    }

    @Test
    void testExtractUsername() {
        String result = jwtService.extractUsername(
            Jwts.builder()
                .setSubject("someuser")
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact()
        );

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("someuser");
    }

    @Test
    void testIsTokenValid() {
        boolean result = jwtService.isTokenValid(
            Jwts.builder()
                .setSubject("someuser")
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hr validity
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact(),
            Reader.builder().username("someuser").build()
        );

        assertThat(result).isTrue();
    }

    @Test
    void testIsTokenValid_MismatchedName() {
        boolean result = jwtService.isTokenValid(
            Jwts.builder()
                .setSubject("someuser")
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact(),
            Reader.builder().username("someotheruser").build()
        );

        assertThat(result).isFalse();
    }

//    @Test
//    void testIsTokenValid_Expired() {
//        // TODO - Gracefully handle exception from expired token
//    }
    // TODO - Add test for exception from missing Reader in token
    @Test
    void testRefreshToken() {
        when(refreshTokenRepo.findByToken(anyString())).thenReturn(
            Optional.of(
                RefreshToken
                    .builder()
                    .reader(
                        Reader
                            .builder()
                            .username("someuser")
                            .build()
                    )
                    .expiryDate(Instant.now().plusMillis(1000L * 60L * 60L))
                    .build()
            )
        );

        ResponseEntity<MessageResponseDTO> result = jwtService.refreshToken("sometoken");

        verify(refreshTokenRepo, times(1)).findByToken(anyString());
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getMessage()).isEqualTo("Token refreshed");
    }

    @Test
    void testRefreshToken_expired() {
        when(refreshTokenRepo.findByToken(anyString())).thenReturn(
            Optional.of(
                RefreshToken
                    .builder()
                    .reader(
                        Reader
                            .builder()
                            .username("someuser")
                            .build()
                    )
                    .expiryDate(Instant.now().minusMillis(1000L * 60L * 60L))
                    .build()
            )
        );

        // Test that the exception is thrown
        assertThatThrownBy(() -> jwtService.refreshToken("sometoken"))
            .isInstanceOf(TokenRefreshException.class)
            .hasMessageContaining("Refresh token expired");
    }

    @Test
    void testRefreshToken_readerNotFound() {
        when(refreshTokenRepo.findByToken(anyString())).thenReturn(Optional.empty());

        // Test that the exception is thrown
        assertThatThrownBy(() -> jwtService.refreshToken("sometoken"))
            .isInstanceOf(TokenRefreshException.class)
            .hasMessageContaining("Refresh token doesn't exist");
    }

    @Test
    void testCreateRefreshToken() {
        when(readerRepo.findById(any(UUID.class))).thenReturn(Optional.of(Reader.builder().username("someuser").build()));

        when(refreshTokenRepo.save(any(RefreshToken.class))).thenReturn(
            RefreshToken
                .builder()
                .token("sometoken")
                .expiryDate(Instant.now().plusMillis(1000L * 60L * 60L))
                .build()
        );

        RefreshToken result = jwtService.createRefreshToken(UUID.randomUUID());

        verify(refreshTokenRepo, times(1)).save(any(RefreshToken.class));
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("sometoken");
        assertThat(result.getExpiryDate()).isNotNull();
    }

    @Test
    void testCreateRefreshToken_readerNotFound() {
        when(readerRepo.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Test that the exception is thrown
        assertThatThrownBy(() -> jwtService.createRefreshToken(UUID.randomUUID()))
            .isInstanceOf(ReaderNotFoundException.class)
            .hasMessageContaining("Reader could not be found");
    }

    @Test
    void testDeleteByReaderID() {
        when(readerRepo.findById(any(UUID.class))).thenReturn(Optional.of(Reader.builder().username("someuser").build()));

        jwtService.deleteByReaderID(UUID.randomUUID());

        verify(refreshTokenRepo, times(1)).deleteByReader(any(Reader.class));
    }

    @Test
    void testDeleteByReaderId_readerNotFound() {
        when(readerRepo.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Test that the exception is thrown
        assertThatThrownBy(() -> jwtService.deleteByReaderID(UUID.randomUUID()))
            .isInstanceOf(ReaderNotFoundException.class)
            .hasMessageContaining("Reader could not be found");
    }
}
