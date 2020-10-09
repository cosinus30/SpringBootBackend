package com.innova.security.jwt;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.innova.exception.AccountNotActivatedException;
import com.innova.model.Attempt;
import com.innova.repository.AttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
            if(attemptRepository.existsByIp(request.getRemoteAddr())) {
                Optional<Attempt> optionalAttempt = attemptRepository.findById(request.getRemoteAddr());
                Attempt attempt=optionalAttempt.get();
                long hour = ChronoUnit.HOURS.between(attempt.getFirst_attempt_date(), LocalDateTime.now());

                if (hour >= 24) {
                    attempt.setAttemptCounter(1);
                    attempt.setFirst_attempt_date(LocalDateTime.now());
                } else {
                    attempt.setAttemptCounter(attempt.getAttemptCounter() + 1);
                }

                attemptRepository.save(attempt);
            } else {
                Attempt attempt = new Attempt(request.getRemoteAddr(), 1, LocalDateTime.now());
                attemptRepository.save(attempt);
            }
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Username or password is incorrect.");
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