package com.bahubba.bahubbabookclub.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Log4j2 // DELETEME
public class JwtService {
    @Value("${app.properties.secret_key}")
    private String secretKey;

    @Value("${app.properties.auth_cookie_name}")
    private String authCookieName;

    @Value("${app.properties.refresh_cookie_name}")
    private String refreshCookieName;

    public ResponseCookie generateJwtCookie(UserDetails userDetails) {
        String jwt = generateToken(new HashMap<>(), userDetails);
        return generateCookie(authCookieName, jwt, "/api");
    }

    public ResponseCookie generateJwtRefreshCookie(String refreshToken) {
        return generateCookie(refreshCookieName, refreshToken, "/api/v1/auth/refresh");
    }

    public String getJwtFromCookies(HttpServletRequest req) {
        log.info("CHECKING COOKIES! " + authCookieName + ": " + getCookieValueByName(req, authCookieName)); // DELETEME
        return getCookieValueByName(req, authCookieName);
    }

    public String getJwtRefreshFromCookies(HttpServletRequest req) {
        return getCookieValueByName(req, refreshCookieName);
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(authCookieName, "").path("/api").build();
    }

    public ResponseCookie getCleanJwtRefreshCookie() {
        return ResponseCookie.from(refreshCookieName, "").path("/api/v1/auth/refresh").build();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60)) // 1 hr validity
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    // TODO - Update validity checks with error handling (see https://www.bezkoder.com/spring-security-refresh-token/)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private ResponseCookie generateCookie(String name, String value, String path) {
        return ResponseCookie.from(name, value).path(path).maxAge(24L * 60L * 60L).httpOnly(true).secure(true).sameSite("None").domain(null).build();
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
