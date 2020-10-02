package com.innova.security.jwt;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.innova.model.Attempt;
import com.innova.repository.AttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthEntryPoint.class);

    @Autowired
    AttemptRepository attemptRepository;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e)
            throws IOException, ServletException {
        String jwt = getJwt(request);
        if(jwt == null){
            try{
                Attempt attempt = attemptRepository.findById(request.getRemoteAddr()).orElseThrow(() ->
                        new UsernameNotFoundException("User Not Ip addr: " + request.getRemoteAddr()));

                long hour=ChronoUnit.HOURS.between(LocalDateTime.now(), attempt.getFirst_attempt_date());

                if(hour>=24){
                    attempt.setAttemptCounter(0);
                    attempt.setFirst_attempt_date(LocalDateTime.now());
                }
                else{
                    attempt.setAttemptCounter(attempt.getAttemptCounter()+1);
                }

                attemptRepository.save(attempt);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Captcha -> ");
            }catch (Exception ex){
                Attempt attempt = new Attempt(request.getRemoteAddr(), 1, LocalDateTime.now());
                attemptRepository.save(attempt);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Captcha -> ");
            }
        }

    }

    private String getJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ","");
        }

        return null;
    }
}