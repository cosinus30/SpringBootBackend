package com.innova.controller;


import com.innova.domain.User;
import com.innova.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    PasswordEncoder encoder;

    @GetMapping("/")
    public List<User> getUsers(){
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id){
        return userService.findById(id);
    }

    @PostMapping("/")
    public User postUser(@RequestBody User user){
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            return userService.save(user);
        }catch(Exception e){
            return null;
        }
    }


}
