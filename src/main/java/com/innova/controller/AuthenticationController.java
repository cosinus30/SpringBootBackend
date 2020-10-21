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
        if(loginForm.getPassword() == null || loginForm.getUsername() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Arrays.asList("Username and password should be provided"));
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

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
    }

    @PostMapping("sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpForm) {
        if (userRepository.existsByUsername(signUpForm.getUsername())) {
            return new ResponseEntity<String>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUpForm.getEmail())) {
            return new ResponseEntity<String>("Email is already in use!", HttpStatus.BAD_REQUEST);
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

        return ResponseEntity.ok().body("User registered successfully!");
    }

    @GetMapping("/confirmRegistration")
    public String confirmRegistration(@RequestParam("token") String token, HttpServletRequest request) {
        if (token == null) {
            return "token not given";
        }

        if (token != null && jwtProvider.validateJwtToken(token, "verification", request)) {
            String username = jwtProvider.getUserNameFromJwtToken(token, "verification");
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
            user.setEnabled(true);
            userRepository.save(user);
            return "Thank you!";
        } else {
            return "Something is wrong!";
        }
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<?> getAccessToken(@RequestParam("token") String token,  HttpServletRequest request){
        if(token == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Arrays.asList("Token cannot be empty"));
        }

        String email = jwtProvider.getUserNameFromJwtToken(token, "refresh");
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }
}