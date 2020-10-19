package com.innova.security.jwt;

import com.innova.model.User;
import com.innova.security.services.UserDetailImpl;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


import java.util.Date;

@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);


    @Value("${innova.app.jwtSecretForAccessToken}")
    private String jwtSecretForAccessToken;
    @Value("${innova.app.jwtAccessTokenExpiration}")
    private int jwtAccessTokenExpiration;

    @Value("${innova.app.jwtSecretForRefreshToken}")
    private String jwtSecretForRefreshToken;
    @Value("${innova.app.jwtRefreshTokenExpiration}")
    private int jwtRefreshTokenExpiration;

    @Value("${innova.app.jwtSecretForVerification}")
    private String jtwSecretForVerification;
    @Value("${innova.app.jwtVerificationTokenExpiration}")
    private int jwtVerificationTokenExpiration;


    public String generateJwtTokenForVerification(User user){
        return Jwts.builder()
                .setSubject((user.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtVerificationTokenExpiration))
                .signWith(SignatureAlgorithm.HS512, jtwSecretForVerification)
                .compact();
    }

    public String generateJwtToken(Authentication authentication) {

        UserDetailImpl userPrincipal = (UserDetailImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtAccessTokenExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecretForAccessToken)
                .compact();
    }

    public String getUserNameFromJwtToken(String token, String matter) {
        String secret = matter.equals("verification") ? jtwSecretForVerification : jwtSecretForAccessToken;

        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken, String matter) {
        String secret = matter.equals("verification") ? jtwSecretForVerification : jwtSecretForAccessToken;

        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature -> Message: {} ", e);
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token -> Message: {}", e);
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token -> Message: {}", e);
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token -> Message: {}", e);
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty -> Message: {}", e);
        }
        return false;
    }
}