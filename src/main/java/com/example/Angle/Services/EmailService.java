package com.example.Angle.Services;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Config.SecServices.JwtService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private final Logger logger = LogManager.getLogger(EmailService.class);

    private final String frontUrl = System.getenv("ANGLE_IP");

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AccountService accountService;

    public void restorePassword(String email, String userIP) {
        try {
            Account toRestore = accountService.getUserByEmail(email);
            String token = jwtService.generatePasswordRecoveryToken(toRestore.getUsername(),userIP);
            String restoreUrl = "/restorePassword?id=";
            String message = "Dear "+toRestore.getUsername()+", \n\n\n\n\n" +
                    "Here is the link to restore your password to your Angle account:\n" + frontUrl+ restoreUrl +token+"\n" +
                    "The link will expire in 15 minutes since your submission. Do not share it!";
            this.sendEmail(toRestore.getEmail(),"Angle: Password Restoration",message);
        } catch (MediaNotFoundException e) {
            logger.error(e.getMessage());
            return;
        }

    }

    public void passwordChangeMail(String username) throws MediaNotFoundException {
        Account account = accountService.getUserByUsername(username);
        String message = "Dear "+username+",\n\n\n\n\n\n" +
                "Your password has been changed. If it wasn't you, please contact our support immediately!\n";
        sendEmail(account.getEmail(),"Angle: Your password has been changed",message);
    }

    public void confirmationEmail(String email) throws MediaNotFoundException {
        Account account = accountService.getUserByEmail(email);
        String token = jwtService.generateEmailConfirmationToken(account.getUsername());
        String message = "Dear "+account.getUsername()+",\n" +
                "Here is your confirmation link to activate your Angle account: \n\n\n\n\n" +
                frontUrl+"/confirmAccount?id="+token;
        sendEmail(account.getEmail(),"Angle: Account Creation Confirmation",message);
    }

    public void sendEmail(String to, String subject, String text){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text+"\n \n \n \n \n This message has been auto generated. Please do not respond.\n Angle Team");
        mailSender.send(mailMessage);
        logger.info("Mail has been sent!");
    }
}
