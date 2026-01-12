package ch.tbz.users.services;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import ch.tbz.users.entities.User;
import ch.tbz.users.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.expiration-time}")
    private long EXPIRATION_TIME;

    private String stripBearerPrefix(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    public String extractClaim(String token) {
        token = stripBearerPrefix(token);
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        token = stripBearerPrefix(token);
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired");
            throw new InvalidTokenException("Token has expired");
        } catch (JwtException e) {
            log.error("Invalid token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token");
        }
    }

    public String generateToken(User userDetails) {
        log.debug("Generating token for user: {}", userDetails.getId());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userDetails.getId().toString());
        extraClaims.put("role", userDetails.getRole().name());
        return buildToken(extraClaims, userDetails.getEmail(), EXPIRATION_TIME);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userId = extractUserId(token);
        return userId.equals(((User) userDetails).getId().toString()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
