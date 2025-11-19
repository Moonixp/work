package org.turntabl.chatapp.security;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get the username from the signed Token
     * This is a function on top of the already existing extractAllCliams
     * 
     * @param token the token which the username is to be extracted from
     * 
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Get the user Id from the signed Token
     * This is a function on top of the already existing extractAllCliams
     * 
     * @param token the token which the user Id is to be extracted from
     * 
     */

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        var userId = claims.get("userId", String.class);
        return UUID.fromString(userId);
    }

    /**
     * This will return a Claims which is map of all the data you inserted into the
     * token at the point of generation
     * 
     * @param token token which the claims will be extracted
     */
    @SuppressWarnings("deprecation")
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, String username) {
        String extracted = extractUsername(token);
        Date expiration = extractAllClaims(token).getExpiration();
        return extracted.equals(username) && expiration.after(new Date());
    }
}
