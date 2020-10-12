package com.innova.event;

import com.innova.model.User;
import com.innova.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class RegistrationEmailListener implements ApplicationListener<OnRegistrationSuccessEvent> {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Override
    public void onApplicationEvent(OnRegistrationSuccessEvent event){
        try {
            this.confirmRegistration(event);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }

    }

    public void sendSimpleMessage(String content, String to, String subject) throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,
                                            MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                                            StandardCharsets.UTF_8.name());

        helper.setTo(to);
        helper.setText(content, true);
        helper.setSubject(subject);

        javaMailSender.send(message);
    }

    private void confirmRegistration(OnRegistrationSuccessEvent event) throws MessagingException, IOException {
        User user = event.getUser();
        String token = jwtProvider.generateJwtTokenForVerification(user);

        String recipient = user.getEmail();
        String url = "http://localhost:8080" + event.getAppUrl() + "/confirmRegistration?token=" + token;

        Map model = new HashMap();
        model.put("name", user.getUsername());
        model.put("url", url);
        model.put("signature", "https://www.innova.com.tr/tr");

        Context context = new Context();
        context.setVariables(model);
        String content = templateEngine.process("db-verification_email", context);

        sendSimpleMessage(content ,recipient, "Registration Confirmation");
    }
}
