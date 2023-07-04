package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.ReaderNotFoundException;
import com.bahubba.bahubbabookclub.exception.TokenRefreshException;
import com.bahubba.bahubbabookclub.model.dto.MessageResponseDTO;
import com.bahubba.bahubbabookclub.model.entity.Reader;
import com.bahubba.bahubbabookclub.model.entity.RefreshToken;
import com.bahubba.bahubbabookclub.repository.ReaderRepo;
import com.bahubba.bahubbabookclub.repository.RefreshTokenRepo;
import com.bahubba.bahubbabookclub.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
@Transactional
public class JwtServiceImpl implements JwtService {
    @Value("${app.properties.secret_key}")
    private String secretKey;

    @Value("${app.properties.auth_cookie_name}")
    private String authCookieName;

    @Value("${app.properties.refresh_cookie_name}")
    private String refreshCookieName;

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Autowired
    private ReaderRepo readerRepo;

    @Override
    public ResponseCookie generateJwtCookie(UserDetails userDetails) {
        String jwt = generateToken(new HashMap<>(), userDetails);
        return generateCookie(authCookieName, jwt, "/api");
    }

    @Override
    public ResponseCookie generateJwtRefreshCookie(String refreshToken) {
        return generateCookie(refreshCookieName, refreshToken, "/api/v1/auth/refresh");
    }

    @Override
    public String getJwtFromCookies(HttpServletRequest req) {
        return getCookieValueByName(req, authCookieName);
    }

    @Override
    public String getJwtRefreshFromCookies(HttpServletRequest req) {
        return getCookieValueByName(req, refreshCookieName);
    }

    @Override
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(authCookieName, "").path("/api").build();
    }

    @Override
    public ResponseCookie getCleanJwtRefreshCookie() {
        return ResponseCookie.from(refreshCookieName, "").path("/api/v1/auth/refresh").build();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hr validity
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    // TODO - Update validity checks with error handling (see https://www.bezkoder.com/spring-security-refresh-token/)
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public ResponseEntity<MessageResponseDTO> refreshToken(String refreshToken) {
        return getByToken(refreshToken)
            .map(this::verifyExpiration)
            .map(RefreshToken::getReader)
            .map(reader -> {
                ResponseCookie jwtCookie = this.generateJwtCookie(reader);

                return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(MessageResponseDTO.builder().message("Token refreshed").build());
            })
            .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token doesn't exist"));
    }

    @Override
    public Optional<RefreshToken> getByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    @Override
    public RefreshToken createRefreshToken(UUID readerID) {
        // Get the current reader
        Reader reader = readerRepo.findById(readerID).orElseThrow(() -> new ReaderNotFoundException(readerID));

        RefreshToken refreshToken = RefreshToken
            .builder()
            .reader(reader)
            .token(UUID.randomUUID().toString())
            .expiryDate(Instant.now().plusMillis(1000L * 60L * 60L))
            .build();

        return refreshTokenRepo.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepo.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token expired");
        }

        return token;
    }

    @Override
    public int deleteByReaderID(UUID readerID) {
        return refreshTokenRepo.deleteByReader(
            readerRepo.findById(readerID)
                .orElseThrow(() -> new ReaderNotFoundException(readerID))
        );
    }

    private ResponseCookie generateCookie(String name, String value, String path) {
        return ResponseCookie.from(name, value).path(path).maxAge(24L * 60L * 60L).httpOnly(true).secure(true).domain(null).sameSite("None").build();
    }

    private String getCookieValueByName(HttpServletRequest req, String name) {
        Cookie cookie = WebUtils.getCookie(req, name);
        if (cookie != null) {
            return cookie.getValue();
        }

        return null;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}