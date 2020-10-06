package com.innova.service;

import com.innova.model.User;

public interface IUserService {
    void createVerificationToken(User user, String token);
}
