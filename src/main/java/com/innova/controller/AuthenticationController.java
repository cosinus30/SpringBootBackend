package com.innova.controller;

import com.innova.aspect.RequiresCaptcha;
import com.innova.event.OnRegistrationSuccessEvent;
import com.innova.exception.AccountNotActivatedException;
import com.innova.exception.ErrorWhileSendingEmailException;
import com.innova.model.Role;
import com.innova.model.Roles;
import com.innova.model.User;
import com.innova.message.request.LoginForm;
import com.innova.message.request.SignUpForm;
import com.innova.message.response.LoginResponse;
import com.innova.repository.AttemptRepository;
import com.innova.repository.RoleRepository;
import com.innova.repository.UserRepository;
import com.innova.security.jwt.JwtProvider;

import com.innova.security.services.UserDetailImpl;
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
import java.net.URISyntaxException;
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
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList());

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
            return new ResponseEntity(HttpStatus.SEE_OTHER);
        }

        if (token != null && jwtProvider.validateJwtToken(token, "verification", request)) {
            String username = jwtProvider.getSubjectFromJwt(token, "verification");
            User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
            user.setEnabled(true);
            userRepository.save(user);

//            URI yahoo = new URI("http://localhost:4200/");
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.setContentLength(3000);
//            httpHeaders.setLocation(yahoo);
//            return new ResponseEntity(httpHeaders, HttpStatus.SEE_OTHER);
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.SEE_OTHER);
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
            response.put("Access token", newAccessToken);
            return ResponseEntity.ok(response);
        }
        else{
            myMap.put("error", "Invalid refresh token");
            myMap.put("status", HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(myMap, HttpStatus.UNAUTHORIZED);
        }
    }
}