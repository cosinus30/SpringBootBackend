package com.innova.controller;

import com.innova.aspect.RequiresCaptcha;
import com.innova.event.OnRegistrationSuccessEvent;
import com.innova.exception.AccountNotActivatedException;
import com.innova.exception.ErrorWhileSendingEmailException;
import com.innova.exception.ForbiddenException;
import com.innova.model.Attempt;
import com.innova.model.Role;
import com.innova.model.Roles;
import com.innova.model.User;
import com.innova.message.request.LoginForm;
import com.innova.message.request.SignUpForm;
import com.innova.message.response.JwtResponse;
import com.innova.repository.RoleRepository;
import com.innova.repository.UserRepository;
import com.innova.security.jwt.JwtProvider;

import com.innova.security.services.UserDetailImpl;
import com.innova.security.services.UserDetailsServiceImpl;
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


import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/auth")
public class AuthenticationController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    @PostMapping("signin")
    @RequiresCaptcha
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginForm, HttpServletResponse response) throws IOException, AccountNotActivatedException {

            Optional<User> user= userRepository.findByUsername(loginForm.getUsername());
            System.out.println(user.get().isEnabled());

            if(!user.get().isEnabled()){
                throw new AccountNotActivatedException("Account has not been activated.");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginForm.getUsername(),
                            loginForm.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtProvider.generateJwtToken(authentication);

            UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
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
        }catch(Exception re) {
			throw new ErrorWhileSendingEmailException(re.getMessage());
        }

        return ResponseEntity.ok().body("User registered successfully!");
    }

    @GetMapping("/confirmRegistration")
    public String confirmRegistration(@RequestParam("token") String token) {
        if(token == null) {
            return "token not given";
        }

        if (token!=null && jwtProvider.validateJwtToken(token, "verification")) {
            String username = jwtProvider.getUserNameFromJwtToken(token, "verification");
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
            user.setEnabled(true);
            userRepository.save(user);
            return "Thank you!";
        }
        else{
            return "Something is wrong!";
        }


    }

}