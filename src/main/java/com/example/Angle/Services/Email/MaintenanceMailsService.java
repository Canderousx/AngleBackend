package com.example.Angle.Services.Email;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Config.Models.EnvironmentVariables;
import com.example.Angle.Config.SecServices.JwtService;
import com.example.Angle.Services.Email.Interfaces.MaintenanceMailsInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MaintenanceMailsService implements MaintenanceMailsInterface {

    private final EmailSenderService emailSenderService;

    private final EnvironmentVariables environmentVariables;

    private final AccountRetrievalService accountRetrievalService;

    private final JwtService jwtService;

    private final Logger logger = LogManager.getLogger(MaintenanceMailsService.class);

    @Autowired
    public MaintenanceMailsService(EmailSenderService emailSenderService,
                                   EnvironmentVariables environmentVariables,
                                   AccountRetrievalService accountRetrievalService,
                                   JwtService jwtService) {
        this.emailSenderService = emailSenderService;
        this.environmentVariables = environmentVariables;
        this.accountRetrievalService = accountRetrievalService;
        this.jwtService = jwtService;
    }

    @Override
    public void restorePassword(String email, String userIP) {
        Account toRestore = accountRetrievalService.getUserByEmail(email);
        String token = jwtService.generatePasswordRecoveryToken(toRestore.getUsername(),userIP);
        String restoreUrl = "/restorePassword?id=";
        String message = "Dear "+toRestore.getUsername()+", \n\n\n\n\n" +
                "Here is the link to restore your password to your Angle account:\n" +
                environmentVariables.getFrontUrl()+ restoreUrl +token+"\n" +
                "The link will expire in 15 minutes since your submission. Do not share it!";
        this.emailSenderService.sendEmail(toRestore.getEmail(),"Angle: Password Restoration",message);
    }

    @Override
    public void passwordChangeMail(String username) throws MediaNotFoundException {
        Account account = accountRetrievalService.getUserByUsername(username);
        String message = "Dear "+username+",\n\n\n\n\n\n" +
                "Your password has been changed. If it wasn't you, please contact our support immediately!\n";
        emailSenderService.sendEmail(account.getEmail(),"Angle: Your password has been changed",message);
    }

    @Override
    public void confirmationEmail(String email) throws MediaNotFoundException {
        Account account = accountRetrievalService.getUserByEmail(email);
        String token = jwtService.generateEmailConfirmationToken(account.getUsername());
        String message = "Dear "+account.getUsername()+",\n" +
                "Here is your confirmation link to activate your Angle account: \n\n\n\n\n" +
                environmentVariables.getFrontUrl()+"/confirmAccount?id="+token;
        emailSenderService.sendEmail(account.getEmail(),"Angle: Account Creation Confirmation",message);
    }
}
