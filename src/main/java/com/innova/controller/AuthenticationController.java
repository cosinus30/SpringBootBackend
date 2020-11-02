package com.innova.controller;

import com.innova.aspect.RequiresCaptcha;
import com.innova.event.OnPasswordForgotEvent;
import com.innova.event.OnRegistrationSuccessEvent;
import com.innova.exception.AccountNotActivatedException;
import com.innova.exception.ErrorWhileSendingEmailException;
import com.innova.model.ActiveSessions;
import com.innova.model.Attempt;
import com.innova.model.Role;
import com.innova.model.Roles;
import com.innova.model.User;
import com.innova.message.request.ForgotPasswordForm;
import com.innova.message.request.LoginForm;
import com.innova.message.request.SignUpForm;
import com.innova.message.response.LoginResponse;
import com.innova.repository.ActiveSessionsRepository;
import com.innova.repository.AttemptRepository;
import com.innova.repository.RoleRepository;
import com.innova.repository.UserRepository;
import com.innova.security.jwt.JwtProvider;

import com.innova.security.services.UserDetailImpl;
import com.innova.util.PasswordUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/auth")
public class AuthenticationController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AttemptRepository attemptRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ActiveSessionsRepository activeSessionsRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping("signin")
    @RequiresCaptcha
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginForm, HttpServletRequest request) throws IOException, AccountNotActivatedException {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("timestamp", new Date());
        myMap.put("path", "api/auth/signin");

        if(loginForm.getPassword() == null || loginForm.getUsername() == null){
            myMap.put("error","Username and password should be provided" );
            myMap.put("status", HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(myMap, HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginForm.getUsername(),
                        loginForm.getPassword()
                )
        );

        User user = userRepository.findByUsername(loginForm.getUsername()).orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));

        if (!user.isEnabled()) {
            throw new AccountNotActivatedException("Account has not been activated.");
        }

        UserDetailImpl userPrincipal = UserDetailImpl.build(user);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtProvider.generateJwtToken(userPrincipal);
        String refreshToken = jwtProvider.generateRefreshToken(authentication, loginForm.isRememberMe());

        UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList());
        if(attemptRepository.existsByIp(request.getRemoteAddr())) {
            Attempt attempt = attemptRepository.findById(request.getRemoteAddr()).get();
            attempt.setAttemptCounter(0);
            attemptRepository.save(attempt);
        }

        String userAgent = request.getHeader("User-Agent") == null ? "Not known" : request.getHeader("User-Agent");

        ActiveSessions activeSession = new ActiveSessions(
            refreshToken,
            userAgent,
            LocalDateTime.ofInstant(jwtProvider.getExpiredDateFromJwt(refreshToken, "refresh").toInstant(), ZoneId.systemDefault()),
            LocalDateTime.ofInstant(jwtProvider.getIssueDateFromJwt(refreshToken, "refresh").toInstant(), ZoneId.systemDefault())
        );

        activeSession.setUser(user);
         
        System.out.println(jwtProvider.getExpiredDateFromJwt(refreshToken, "refresh"));
        System.out.println(jwtProvider.getIssueDateFromJwt(refreshToken, "refresh"));
        

        activeSessionsRepository.save(activeSession);

        return ResponseEntity.ok(new LoginResponse(accessToken,
                refreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                userDetails.getName(),
                userDetails.getLastName()));
    }

    @PostMapping("sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpForm) {
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("timestamp", new Date());
        myMap.put("path", "api/auth/sign-up");
        if (userRepository.existsByUsername(signUpForm.getUsername())) {
            myMap.put("status", HttpStatus.BAD_REQUEST.value());
            myMap.put("error", "Username is already taken!");
            return new ResponseEntity<>(myMap, HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUpForm.getEmail())) {
            myMap.put("status", HttpStatus.BAD_REQUEST.value());
            myMap.put("error", "Email is already in use!");
            return new ResponseEntity<>(myMap, HttpStatus.BAD_REQUEST);
        }

        if (!PasswordUtil.isValidPassword(signUpForm.getPassword())){
            myMap.put("status", HttpStatus.BAD_REQUEST.value());
            myMap.put("error", "Password is not valid. It should have at least one uppercase, lowercase letter, number. Min length is 6");
            return new ResponseEntity<>(myMap, HttpStatus.BAD_REQUEST);
        }

        User user = new User(signUpForm.getUsername(), signUpForm.getEmail(), passwordEncoder.encode(signUpForm.getPassword()));
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByRole(Roles.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
        roles.add(userRole);
        user.setRoles(roles);
        userRepository.save(user);

        try {
            eventPublisher.publishEvent(new OnRegistrationSuccessEvent(user, "/api/auth"));
        } catch (Exception re) {
            throw new ErrorWhileSendingEmailException(re.getMessage());
        }
        myMap.put("status", HttpStatus.CREATED.value());
        myMap.put("message", "User registered successfully!");
        return new ResponseEntity<>(myMap, HttpStatus.CREATED);
    }

    @GetMapping("/confirmRegistration")
    public ResponseEntity<?> confirmRegistration(@RequestParam("token") String token, HttpServletRequest request) throws URISyntaxException {
        if (token == null) {
            //Token is empty redirect to error or something
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(URI.create("http://localhost:4200"))
                    .build();
        }

        if (token != null && jwtProvider.validateJwtToken(token, "verification", request)) {
            String username = jwtProvider.getSubjectFromJwt(token, "verification");
            User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
            user.setEnabled(true);
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(URI.create("http://localhost:4200"))
                    .build();
        } else {
            // Token is not valid
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(URI.create("http://localhost:4200"))
                    .build();
        }
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<?> getAccessToken(@RequestParam("token") String token,  HttpServletRequest request){
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("timestamp", new Date());
        myMap.put("path", "api/auth/refresh-token");
        if(token == null){
            myMap.put("error", "Token cannot be empty");
            myMap.put("status", HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(myMap, HttpStatus.BAD_REQUEST);
        }

        String email = jwtProvider.getSubjectFromJwt(token, "refresh");
        System.out.println(email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Fail! -> Cause: Email not found."));
        if (!user.isEnabled()) {
            throw new AccountNotActivatedException("Account has not been activated.");
        }
        UserDetailImpl userPrincipal = UserDetailImpl.build(user);

        if(jwtProvider.validateJwtToken(token, "refresh", request)){
            Map<String, Object> response = new HashMap<>();
            String newAccessToken = jwtProvider.generateJwtToken(userPrincipal);
            response.put("accessToken", newAccessToken);
            return ResponseEntity.ok(response);
        }
        else{
            myMap.put("error", "Invalid refresh token");
            myMap.put("status", HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(myMap, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordForm forgotPasswordForm){
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("timestamp", new Date());
        myMap.put("path", "api/auth/forgot-password");
        String email = forgotPasswordForm.getEmail();

        if(!userRepository.existsByEmail(email)){
            myMap.put("error", "No such user");
            myMap.put("status", HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(myMap, HttpStatus.BAD_REQUEST);
        }
        else{
            try {
                eventPublisher.publishEvent(new OnPasswordForgotEvent(email));
                myMap.put("message", "Email successfuly sent.");
                myMap.put("status", HttpStatus.OK.value());
                return new ResponseEntity<>(myMap, HttpStatus.OK);
            } catch (Exception re) {
                throw new ErrorWhileSendingEmailException(re.getMessage());
            }
        }
    }
}


/**
 *  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrcm10cmsyMEBnbWFpbC5jb20iLCJpYXQiOjE2MDM3ODUyMTQsImV4cCI6MTYwMzc4NzAxNH0.KMAR1bnZnjdpd6Rl0MIxTs0pSXTCdnQefphMkOLJ_Sxw3gh0sFeUSIWt0UqDtgEwikdyp6r-fMz-jlzNcjKzoA",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrcm10cmsyMEBnbWFpbC5jb20iLCJpYXQiOjE2MDM3ODUyMTQsImV4cCI6MTYwNjM3NzIxNH0.y6MeViZDtBohalBIMHHt9NQAtf3JUadOtd4V-iugEMxxQo5hahw9I_tijYg94f0mYfgynYhZdw7TJihXa_wR2w",
 */