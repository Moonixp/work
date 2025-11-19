package org.turntabl.auth.security;

import java.security.Key;
import java.util.Date;
import java.util.Map;
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

    /**
     * This will generate a jwt token
     * 
     * @param extraClaims a map of the extra data you want in the token to easily
     *                    have access to it by default puts in the userId
     * @param subject     the header which should be the email
     */
    public String generateToken(String subject, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        String extracted = extractUsername(token);
        Date expiration = extractAllClaims(token).getExpiration();
        return extracted.equals(username) && expiration.after(new Date());
    }

    /**
     * Generate refresh token
     * todo...
     */

    @Value("${app.jwt.refresh-expiration-ms:604800000}") // 7 days
    private long refreshExpirationMs;

    public String generateRefreshToken(String subject, UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .subject(subject)
                .claim("userId", userId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }
}
