package com.cumulations.music_player.components;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtComponent {
    private static final Logger logger = LoggerFactory.getLogger(JwtComponent.class);

    @Value("${cumulations.app.jwtSecret}")
    private String secrete;

    @Value("${cumulations.app.jwtExpirationMs}")
    private Long expirationMs;

    @Value("${cumulations.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    public String generateJwtRefreshToken(String token){
        return  generateJwtToken(token,refreshTokenDurationMs);
    }

    public String generateJwtToken(String userName){
        return  generateJwtToken(userName,expirationMs);
    }

    public String generateJwtToken(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        logger.warn(user.getUsername());
        return  generateJwtToken(user.getUsername(),expirationMs);
    }

    private String generateJwtToken(String subject,Long expirationMs) {

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secrete));
    }

    public String getSubjectFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public long getIssuedTime(String token){
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getIssuedAt().getTime();
    }

    public long getExpiredAT(String token){
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getExpiration().getTime();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}