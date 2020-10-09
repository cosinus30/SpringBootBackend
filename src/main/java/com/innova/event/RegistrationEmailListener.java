package com.innova.event;

import com.innova.dto.Mail;
import com.innova.model.User;
import com.innova.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public void sendSimpleMessage(Mail mail) throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        Context context = new Context();
        context.setVariables(mail.getModel());
        String html = templateEngine.process("verification-email", context);

        helper.setTo(mail.getTo());
        helper.setText(html, true);
        helper.setSubject(mail.getSubject());

        javaMailSender.send(message);
    }

    private void confirmRegistration(OnRegistrationSuccessEvent event) throws MessagingException, IOException {
        User user = event.getUser();
        String token = jwtProvider.generateJwtTokenForVerification(user);

        String recipient = user.getEmail();
        String url = "http://localhost:8080" + event.getAppUrl() + "/confirmRegistration?token=" + token;

        //TODO Take these values from database
        //TODO Find a solution for caching mail content

        Mail mail = new Mail();
        mail.setTo(recipient);
        mail.setSubject("Registration Confirmation");

        Map model = new HashMap();
        model.put("name", user.getUsername());
        model.put("url", url);
        model.put("signature", "https://www.innova.com.tr/tr");
        mail.setModel(model);

        sendSimpleMessage(mail);
    }
}
