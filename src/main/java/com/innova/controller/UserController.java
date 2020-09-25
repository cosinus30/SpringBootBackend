package com.innova.controller;


import com.innova.domain.User;
import com.innova.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

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
            return userService.save(user);
        }catch(Exception e){
            return null;
        }
    }


}
