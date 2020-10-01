package com.innova.security.captcha;

import com.innova.model.Attempt;
import com.innova.repository.AttemptRepository;
import com.innova.security.jwt.JwtAuthTokenFilter;
import com.innova.security.jwt.JwtProvider;
import com.innova.security.services.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CaptchaFilter extends GenericFilterBean {
    @Autowired
    AttemptRepository attemptRepository;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = getJwt(request);

        if(jwt == null){
            try{
                Attempt attempt = attemptRepository.findById(request.getRemoteAddr()).orElseThrow(() ->
                        new UsernameNotFoundException("User Not Ip addr: " + request.getRemoteAddr()));
                attempt.setAttemptCounter(attempt.getAttemptCounter() + 1);
                attemptRepository.save(attempt);
            }catch (Exception e){
                System.out.println(request.getRemoteAddr());
                Attempt attempt = new Attempt(request.getRemoteAddr(), 0);
                System.out.println(attempt);
                attemptRepository.save(attempt);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }


    private String getJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ","");
        }

        return null;
    }

}
