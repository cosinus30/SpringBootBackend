package com.innova.controller;


import com.innova.event.OnRegistrationSuccessEvent;
import com.innova.exception.ErrorWhileSendingEmailException;
import com.innova.message.request.changeForm;
import com.innova.model.TokenBlacklist;
import com.innova.model.User;
import com.innova.repository.TokenBlacklistRepository;
import com.innova.repository.UserRepository;
import com.innova.security.services.UserDetailImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;

@RestController
@RequestMapping("api/user")
public class UserController {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping("/")
    public ResponseEntity<?> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();
            return ResponseEntity.ok().body(userDetails);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Arrays.asList("Please sign in for retrieving user information."));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editUser(@Valid @RequestBody changeForm changeForm) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();

            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Fail! -> Cause: User cannot find"));
            if (changeForm.getEmail() != null || changeForm.getPassword() != null || changeForm.getAge() != null || changeForm.getName() != null || changeForm.getLastname() != null || changeForm.getPhoneNumber() != null) {
                if (changeForm.getEmail() != null) {
                    if (userRepository.existsByEmail(changeForm.getEmail())) {
                        return new ResponseEntity<String>("Email is already in use!", HttpStatus.BAD_REQUEST);
                    }
                    user.setEmail(changeForm.getEmail());
                    user.setEnabled(false);
                    try {
                        eventPublisher.publishEvent(new OnRegistrationSuccessEvent(user, "/api/auth"));
                    } catch (Exception re) {
                        throw new ErrorWhileSendingEmailException(re.getMessage());
                    }
                }
                if (changeForm.getPassword() != null) {
                    user.setPassword(passwordEncoder.encode(changeForm.getPassword()));
                }
                if (changeForm.getName() != null)
                    user.setName(changeForm.getName());
                if (changeForm.getLastname() != null)
                    user.setLastname(changeForm.getLastname());
                if (changeForm.getAge() != null)
                    user.setAge(changeForm.getAge());
                if (changeForm.getPhoneNumber() != null) {
                    if (changeForm.getPhoneNumber().length() == 10)
                        user.setPhoneNumber(changeForm.getPhoneNumber());
                    else
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Arrays.asList("Size of phone Number need to be equal 10"));
                }
                userRepository.save(user);

                return ResponseEntity.ok(Arrays.asList("User details successfuly changed."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Arrays.asList("You are not changing anything."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Arrays.asList("Please sign in for retrieving user information."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody LogoutForm logoutForm){
            if(logoutForm.getAccessToken() == null || logoutForm.getRefreshToken() == null){
                return ResponseEntity.badRequest().body("Both tokens should be provided");
            }
            else{
                TokenBlacklist oldAccessToken = new TokenBlacklist(logoutForm.getAccessToken(), "access token");
                TokenBlacklist oldRefreshToken = new TokenBlacklist(logoutForm.getRefreshToken(), "refresh token");
                tokenBlacklistRepository.save(oldAccessToken);
                tokenBlacklistRepository.save(oldRefreshToken);
                return ResponseEntity.ok().body("Successfully logged out");
            }

    }
}
