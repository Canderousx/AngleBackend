package com.example.Angle.SecurityTests.ServicesTests;


import com.example.Angle.Config.Exceptions.CredentialExistsException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.UserRole;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Config.SecServices.Account.Interfaces.AccountRetrievalServiceInterface;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Services.Images.ImageRetrievalService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountRetrievalServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ImageRetrievalService imageRetrievalService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;


    @InjectMocks
    private AccountRetrievalService accountRetrievalService;


    private Account account;


    void simulateLoggedInUser(){
        UserDetails userDetails = new User(account.getUsername(), account.getPassword(), Collections.emptyList());
        when(authentication.getName()).thenReturn(userDetails.getUsername());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }


    @BeforeEach
    void setUp(){
        //test account
        account = new Account();
        account.setId("ID");
        account.setUsername("Test");
        account.setEmail("Test@Test.com");
        account.setPassword("testttest");
        Set<UserRole> roles = new HashSet<>();
        account.setRoles(roles);

    }

    @Test
    void usernameExistsTest_Exists() throws CredentialExistsException {
        String username = "existingUser";
        when(accountRepository.existsByUsername(username)).thenReturn(true);
        assertThrows(CredentialExistsException.class, () ->{
            accountRetrievalService.usernameExists(username);
        });
        verify(accountRepository,times(1)).existsByUsername(username);
    }

    @Test
    void usernameExistsTest_notExist(){
        String username = "xyz";
        when(accountRepository.existsByUsername(username)).thenReturn(false);
        assertDoesNotThrow(() ->{
            boolean result = accountRetrievalService.usernameExists(username);
            assertFalse(result);
        });
        verify(accountRepository,times(1)).existsByUsername(username);
    }

    @Test
    void emailExistsTest_Exists(){
        String email = "abc@abc.com";
        when(accountRepository.existsByEmail(email)).thenReturn(true);
        assertThrows(CredentialExistsException.class, () ->{
            accountRetrievalService.emailExists(email);
        });
        verify(accountRepository,times(1)).existsByEmail(email);
    }

    @Test
    void emailExistsTest_notExist(){
        String email = "xyz";
        when(accountRepository.existsByEmail(email)).thenReturn(false);
        assertDoesNotThrow(() ->{
            boolean result = accountRetrievalService.emailExists(email);
            assertFalse(result);
        });
        verify(accountRepository,times(1)).existsByEmail(email);
    }

    @Test
    void isActiveTest_Active(){
        when(accountRepository.isActive(account.getEmail())).thenReturn(true);
        boolean result = accountRetrievalService.isActive(account.getEmail());
        assertTrue(result);
        verify(accountRepository,times(1)).isActive(account.getEmail());
    }

    @Test
    void isActiveTest_NonActive(){
        when(accountRepository.isActive(account.getEmail())).thenReturn(false);
        boolean result = accountRetrievalService.isActive(account.getEmail());
        assertFalse(result);
        verify(accountRepository,times(1)).isActive(account.getEmail());
    }


    @Test
    void isAdmin_Admin() throws BadRequestException {
        UserRole userRole = new UserRole();
        userRole.setName("ROLE_ADMIN");
        account.getRoles().add(userRole);
        simulateLoggedInUser();
        when(accountRepository.findByUsername(account.getUsername())).thenReturn(Optional.of(account));
        boolean result = accountRetrievalService.isAdmin();
        assertTrue(result);
        verify(accountRepository, times(1)).findByUsername(account.getUsername());

    }

    @Test
    void isAdmin_NotAdmin() throws BadRequestException {
        UserRole userRole = new UserRole();
        userRole.setName("ROLE_USER");
        account.getRoles().add(userRole);
        simulateLoggedInUser();
        when(accountRepository.findByUsername(account.getUsername())).thenReturn(Optional.of(account));
        boolean result = accountRetrievalService.isAdmin();
        assertFalse(result);
        verify(accountRepository, times(1)).findByUsername(account.getUsername());

    }

    @Test
    void generateAccountResponseTest_AccountExists() throws IOException, ClassNotFoundException {
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(imageRetrievalService.getImage(account.getAvatar())).thenReturn(new Thumbnail());

        AccountRetrievalServiceInterface.AccountRecord response = accountRetrievalService.generateAccountResponse(account.getId());
        assertEquals(account.getId(),response.id());
        assertEquals(account.getAvatar(),response.avatar());
        assertEquals(account.getSubscribers().size(),response.subscribers());

        verify(accountRepository, times(1)).findById(account.getId());
        verify(imageRetrievalService, times(1)).getImage(account.getAvatar());
    }

    @Test
    void generateAccountResponseTest_AccountNotExist(){
        when(accountRepository.findById(account.getId())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,() ->{
            accountRetrievalService.generateAccountResponse(account.getId());
        });
        verify(accountRepository, times(1)).findById(account.getId());
    }










}
