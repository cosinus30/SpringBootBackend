package com.innova.controller;

import com.innova.message.request.SignUpForm;
import com.innova.model.User;
import com.innova.repository.UserRepository;
import com.innova.security.services.UserDetailImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/")
    public ResponseEntity<?> getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")){
            UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();
            return ResponseEntity.ok().body(userDetails);
        }
        else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Arrays.asList("Please sign in for retrieving user information."));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editUser(@Valid @RequestBody SignUpForm signUpForm){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")){
            UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();

           User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Fail! -> Cause: User cannot find"));
            if(signUpForm.getEmail() != null || signUpForm.getPassword() != null) {
                if (signUpForm.getEmail() != null) {
                    if (userRepository.existsByEmail(signUpForm.getEmail())) {
                        return new ResponseEntity<String>("Email is already in use!", HttpStatus.BAD_REQUEST);
                    }
                    user.setEmail(signUpForm.getEmail());
                }
                if (signUpForm.getPassword() != null) {
                    user.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
                }
                userRepository.save(user);

                return ResponseEntity.ok(Arrays.asList("User details successfuly changed."));
            }
            else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Arrays.asList("You are not changing anything."));
            }
        }
        else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Arrays.asList("Please sign in for retrieving user information."));
        }
    }
}
