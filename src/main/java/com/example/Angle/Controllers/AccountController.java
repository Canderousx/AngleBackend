package com.example.Angle.Controllers;


import com.example.Angle.Config.Exceptions.EmailExistsException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Exceptions.TokenExpiredException;
import com.example.Angle.Config.Exceptions.CredentialExistsException;
import com.example.Angle.Config.Models.*;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Config.SecServices.Account.Interfaces.AccountRetrievalServiceInterface;
import com.example.Angle.Config.SecServices.Account.UserAccountService;
import com.example.Angle.Config.SecServices.JwtService;
import com.example.Angle.Services.Email.MaintenanceMailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("")
public class AccountController {

    private final JwtService jwtService;
    private final AccountAdminService accountAdminService;

    private final AccountRetrievalService accountRetrievalService;

    private final MaintenanceMailsService maintenanceMailsService;

    @Autowired
    public AccountController(JwtService jwtService, AccountAdminService accountAdminService,
                             AccountRetrievalService accountRetrievalService,
                             MaintenanceMailsService maintenanceMailsService) {
        this.jwtService = jwtService;
        this.accountAdminService = accountAdminService;
        this.accountRetrievalService = accountRetrievalService;
        this.maintenanceMailsService = maintenanceMailsService;
    }


    @RequestMapping(value = "/unAuth/getAccount",method = RequestMethod.GET)
    public AccountRetrievalServiceInterface.AccountRecord getAccount(@RequestParam String id) throws IOException, ClassNotFoundException {
        return accountRetrievalService.generateAccountResponse(id);
    }

    @RequestMapping(value = "/isAdmin",method = RequestMethod.GET)
    public boolean isAdmin() throws BadRequestException {
        return accountRetrievalService.isAdmin();
    }

    @RequestMapping(value = "/getMyId",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse> getUserId() throws BadRequestException {
        return ResponseEntity.ok(new SimpleResponse(accountRetrievalService.getCurrentUser().getId()));
    }

    @RequestMapping(value = "/getCurrentUser",method = RequestMethod.GET)
    public AccountRetrievalServiceInterface.AccountRecord getCurrentUser() throws IOException, ClassNotFoundException {
        return accountRetrievalService.generateAccountResponse(accountRetrievalService.getCurrentUser());
    }

    @RequestMapping(value = "/changeAvatar",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>changeAvatar(@RequestParam String id,
                                                      @RequestParam(name = "file") MultipartFile avatar) throws IOException {
        accountAdminService.changeAvatar(id,avatar);
        return ResponseEntity.ok(new SimpleResponse("Avatar has been changed successfully"));
    }


    @RequestMapping(value = "/unAuth/passwordRecovery",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>forgotPassword(@RequestBody String email,
                                                        HttpServletRequest request){
        maintenanceMailsService.restorePassword(email,request.getRemoteAddr());
        return ResponseEntity.ok(new SimpleResponse("If account exists, you should receive a password reset instruction on your email"));
    }

    @RequestMapping(value = "/unAuth/restorePassword",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>restorePassword(@RequestBody PasswordRestoreRequest passwordRequest,
                                                         HttpServletRequest request) throws TokenExpiredException {
        accountAdminService.restorePassword(passwordRequest,request);
        return ResponseEntity.ok(new SimpleResponse("Password has been changed! You can now sign in!"));
    }

    @RequestMapping(value = "unAuth/login",method = RequestMethod.POST)
    public AuthRes login(@RequestBody AuthReq authReq,
                         HttpServletRequest request) throws IOException, MediaNotFoundException {
        return accountAdminService.login(authReq,request);
    }

    @RequestMapping(value = "/unAuth/signup",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>signup(@RequestBody Account account) throws MediaNotFoundException, CredentialExistsException {
        accountAdminService.singup(account);
        return ResponseEntity.ok(new SimpleResponse("In order to login you need to confirm your email. Check your mailbox"));
    }

    @RequestMapping(value = "/unAuth/signup/checkEmail",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>checkEmail(@RequestBody String email) throws CredentialExistsException {
        accountRetrievalService.emailExists(email);
        return ResponseEntity.ok(new SimpleResponse("Email not found!"));
    }

    @RequestMapping(value = "/unAuth/signup/confirmAccount",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>confirmAccount(@RequestParam String token) throws MediaNotFoundException, BadRequestException {
        accountAdminService.confirmAccount(token);
        return ResponseEntity.ok(new SimpleResponse("Email has been confirmed! You're able to login now."));
    }

    @RequestMapping(value = "/unAuth/signup/checkUsername",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>checkUsername(@RequestBody String username) throws CredentialExistsException {
        accountRetrievalService.usernameExists(username);
        return ResponseEntity.ok(new SimpleResponse("Username not found!"));
    }

    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>logout(@RequestBody String token){
        this.jwtService.invalidateToken(token);
        return ResponseEntity.ok(new SimpleResponse("You've been logout!"));
    }


}
