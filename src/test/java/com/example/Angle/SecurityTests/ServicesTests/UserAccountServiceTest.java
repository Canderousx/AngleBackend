package com.example.Angle.SecurityTests.ServicesTests;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Config.SecServices.Account.UserAccountService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountRetrievalService accountRetrievalService;

    @InjectMocks
    private UserAccountService userAccountService;


    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId("userId");
        account.setActive(true);
        account.setPassword(passwordEncoder.encode("abc"));
        account.setLikedVideos(new ArrayList<>());
        account.setDislikedVideos(new ArrayList<>());
        account.setConfirmed(true);
    }

    @Test
    void changeUserPasswordTest(){
        String newPassword = "xyz";
        String encodedNewPassword = "hrthrth";
        when(accountRetrievalService.getUserByUsername(account.getUsername())).thenReturn(account);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        userAccountService.changeUserPassword(account.getUsername(),newPassword);

        assertEquals(encodedNewPassword,account.getPassword());

        verify(accountRetrievalService, times(1)).getUserByUsername(account.getUsername());
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void checkEmailConfirmation_unConfirmed(){
        account.setConfirmed(false);
        when(accountRetrievalService.getUserByEmail(account.getEmail())).thenReturn(account);

        assertThrows(BadRequestException.class,() ->{
            userAccountService.checkEmailConfirmation(account.getEmail());
        });

        verify(accountRetrievalService,times(1)).getUserByEmail(account.getEmail());

    }

    @Test
    void checkEmailConfirmation_confirmed(){
        when(accountRetrievalService.getUserByEmail(account.getEmail())).thenReturn(account);
        assertDoesNotThrow(() -> userAccountService.checkEmailConfirmation(account.getEmail()));
        verify(accountRetrievalService,times(1)).getUserByEmail(account.getEmail());

    }




}
