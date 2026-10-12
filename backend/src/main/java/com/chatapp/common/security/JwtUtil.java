
package com.chatapp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN = "ACCESS";
    private static final String REFRESH_TOKEN = "REFRESH";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN);

        return createToken(
                claims,
                userDetails.getUsername(),
                expiration
        );
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN);

        return createToken(
                claims,
                userDetails.getUsername(),
                refreshExpiration
        );
    }

    private String createToken(
            Map<String, Object> claims,
            String subject,
            Long tokenExpiration
    ) {
        Date now = new Date();

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(
                        new Date(now.getTime() + tokenExpiration)
                )
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(
            String token,
            UserDetails userDetails
    ) {
        return validateTokenType(token, userDetails, ACCESS_TOKEN);
    }

    public boolean validateRefreshToken(
            String token,
            UserDetails userDetails
    ) {
        return validateTokenType(token, userDetails, REFRESH_TOKEN);
    }

    private boolean validateTokenType(
            String token,
            UserDetails userDetails,
            String expectedType
    ) {
        if (token == null || userDetails == null) {
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            Date tokenExpiration = claims.getExpiration();

            return Objects.equals(
                    username,
                    userDetails.getUsername()
            )
                    && Objects.equals(tokenType, expectedType)
                    && tokenExpiration != null
                    && tokenExpiration.after(new Date());

        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}
