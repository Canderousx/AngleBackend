package com.example.Angle.Controllers.Unauth;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Exceptions.TokenExpiredException;
import com.example.Angle.Config.Models.AuthReq;
import com.example.Angle.Config.Models.AuthRes;
import com.example.Angle.Config.Models.PasswordRestoreRequest;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
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

import java.io.IOException;

@RestController
@RequestMapping(value = "/unAuth")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
public class Login {

    private final Logger logger = LogManager.getLogger(Login.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserAccountService userAccountService;

    @Autowired
    AccountRetrievalService accountRetrievalService;

    @Autowired
    MaintenanceMailsService maintenanceMailsService;


    @RequestMapping(value = "/passwordRecovery",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>forgotPassword(@RequestBody String email,
                                                        HttpServletRequest request){
        maintenanceMailsService.restorePassword(email,request.getRemoteAddr());
        return ResponseEntity.ok(new SimpleResponse("If account exists, you should receive a password reset instruction on your email"));
    }

    @RequestMapping(value = "/restorePassword",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>restorePassword(@RequestBody PasswordRestoreRequest passwordRequest,
                                                         HttpServletRequest request) throws MediaNotFoundException, TokenExpiredException {
        String token = passwordRequest.getToken();
        try {
            if (jwtService.validatePasswordRecoveryToken(token, request.getRemoteAddr())) {
                String username = jwtService.extractUsername(token);
                userAccountService.changeUserPassword(username, passwordRequest.getNewPassword());
                maintenanceMailsService.passwordChangeMail(username);
                jwtService.invalidateToken(token);
                return ResponseEntity.ok(new SimpleResponse("Password has been changed! You can now sign in!"));
            }
        }catch (ExpiredJwtException e){
            throw new TokenExpiredException("Access denied. Please try again.");
        }
        return null;
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public AuthRes login(@RequestBody AuthReq authReq,
                         HttpServletRequest request) throws IOException, MediaNotFoundException {
        String userIP = request.getRemoteAddr();
        if(!accountRetrievalService.emailExists(authReq.getEmail())){
            throw new BadCredentialsException("Incorrect username or password!");
        }
        if(!accountRetrievalService.isActive(authReq.getEmail())){
            throw new BadRequestException("Account banned!");
        }
        try {
            userAccountService.checkEmailConfirmation(authReq.getEmail());
        } catch (BadRequestException e) {
            maintenanceMailsService.confirmationEmail(authReq.getEmail());
            throw new BadRequestException(e.getMessage());
        } catch (MediaNotFoundException e) {
            throw new BadRequestException("Account doesn't exist");
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authReq.getEmail(),authReq.getPassword())
        );
        if(authentication.isAuthenticated()){
            logger.info("Generating TOKEN");
            return AuthRes.builder()
                    .authToken(jwtService.generateToken(authReq.getEmail(),userIP))
                    .build();
        }else{
            throw new BadCredentialsException("Incorrect username or password!");
        }

    }


}
