package com.innova.security.jwt;

import com.innova.model.User;
import com.innova.repository.TokenBlacklistRepository;
import com.innova.security.services.UserDetailImpl;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


import java.util.Date;

@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @Autowired
    TokenBlacklistRepository tokenBlacklistRepository;

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

    public String generateRefreshToken(Authentication authentication) {
        UserDetailImpl userPrincipal = (UserDetailImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtRefreshTokenExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecretForRefreshToken)
                .compact();
    }

    public String generateJwtToken(Authentication authentication) {

        StringBuilder payload = new StringBuilder();
        UserDetailImpl userPrincipal = (UserDetailImpl) authentication.getPrincipal();

        payload.append(userPrincipal.getId());
        payload.append(userPrincipal.getAuthorities());
        payload.append(userPrincipal.getName());

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtAccessTokenExpiration))
                .setId(userPrincipal.getId().toString())
                .setPayload(String.valueOf(payload))
                .signWith(SignatureAlgorithm.HS512, jwtSecretForAccessToken)
                .compact();
    }

    public String getUserNameFromJwtToken(String token, String matter) {
        String secret = getSecret(matter);

        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken, String matter) {
        String secret = getSecret(matter);
        try {
            if(matter.equals("refresh") && checkExistence(authToken)){
                return false;
            }
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

    private boolean checkExistence(String token){
        return tokenBlacklistRepository.existsByToken(token);
    }

    private String getSecret(String matter){
        switch (matter){
            case "verification":
                return jtwSecretForVerification;
            case "authorize":
                return jwtSecretForAccessToken;
            case "refresh":
                return jwtSecretForRefreshToken;
            default:
                return null;
        }
    }
}