package com.innova.controller;

import com.innova.security.services.UserDetailImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("api/user")
public class UserController {

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
}
