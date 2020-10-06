package com.innova.event;

import com.innova.event.OnRegistrationSuccessEvent;
import com.innova.model.User;
import com.innova.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


import java.util.UUID;

@Component
public class RegistrationEmailListener implements ApplicationListener<OnRegistrationSuccessEvent> {

    @Autowired
    private MailSender mailSender;

    @Autowired
    private IUserService userService;

    @Override
    public void onApplicationEvent(OnRegistrationSuccessEvent event) {
        this.confirmRegistration(event);

    }

    private void confirmRegistration(OnRegistrationSuccessEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.createVerificationToken(user,token);

        String recipient = user.getEmail();
        String subject = "Registration Confirmation";
        String url = event.getAppUrl() + "/confirmRegistration?token=" + token;
        String message = "Thank you for registering. Please click on the below link to activate your account.";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipient);
        email.setSubject(subject);
        email.setText(message + "http://localhost:8080" + url);
        mailSender.send(email);

    }
}
