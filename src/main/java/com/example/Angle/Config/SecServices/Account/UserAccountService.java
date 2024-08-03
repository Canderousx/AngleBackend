package com.example.Angle.Config.SecServices.Account;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.Account.Interfaces.UserAccountServiceInterface;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserAccountService implements UserAccountServiceInterface {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final AccountRetrievalService accountRetrievalService;

    private final Logger log = LogManager.getLogger(UserAccountService.class);


    @Autowired
    public UserAccountService(AccountRepository accountRepository,
                              PasswordEncoder passwordEncoder,
                              AccountRetrievalService accountRetrievalService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRetrievalService = accountRetrievalService;
    }

    @Override
    public void changeUserPassword(String username, String newPassword) throws MediaNotFoundException {
        Account toChange = accountRetrievalService.getUserByUsername(username);
        toChange.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(toChange);
    }

    @Override
    public void checkEmailConfirmation(String email) throws MediaNotFoundException, BadRequestException {
        Account account = accountRetrievalService.getUserByEmail(email);
        if (!account.isConfirmed()) {
            log.info("Email: " + email + " not confirmed!");
            throw new BadRequestException("You need to confirm your email address. Check your mailbox!");

        }
    }
}
