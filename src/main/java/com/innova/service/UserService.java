package com.innova.service;

import com.innova.model.EmailVerificationToken;
import com.innova.model.User;
import com.innova.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    @Autowired
    EmailVerificationTokenRepository tokenRepository;

    @Override
    public void createVerificationToken(User user, String token) {
        EmailVerificationToken newUserToken = new EmailVerificationToken(token, user);
        tokenRepository.save(newUserToken);
    }
}
