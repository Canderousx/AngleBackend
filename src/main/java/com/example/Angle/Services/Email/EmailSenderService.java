package com.example.Angle.Services.Email;

import com.example.Angle.Services.Email.Interfaces.EmailSenderInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailSenderService implements EmailSenderInterface {

    private final JavaMailSender mailSender;

    private final Logger logger = LogManager.getLogger(EmailSenderService.class);

    @Autowired
    public EmailSenderService(JavaMailSender javaMailSender) {
        this.mailSender = javaMailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text+"\n \n \n \n \n This message has been auto generated. Please do not respond.\n Angle Team");
        mailSender.send(mailMessage);
        logger.info("Mail to {"+to+"} has been sent!");
    }
}
