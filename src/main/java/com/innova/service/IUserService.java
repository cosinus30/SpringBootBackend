package com.innova.service;

import com.innova.model.User;
import org.springframework.stereotype.Service;

@Service
public interface IUserService {
    void createVerificationToken(User user, String token);
}
