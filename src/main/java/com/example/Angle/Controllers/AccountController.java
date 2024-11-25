package com.example.Angle.Controllers;


import com.example.Angle.Config.Exceptions.EmailExistsException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Exceptions.TokenExpiredException;
import com.example.Angle.Config.Exceptions.UsernameExistsException;
import com.example.Angle.Config.Models.*;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.UserRoleRepository;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Config.SecServices.Account.Interfaces.AccountRetrievalServiceInterface;
import com.example.Angle.Config.SecServices.Account.UserAccountService;
import com.example.Angle.Config.SecServices.JwtService;
import com.example.Angle.Services.Email.MaintenanceMailsService;
import com.example.Angle.Services.Images.ImageConverterService;
import com.example.Angle.Services.Images.ImageSaveService;
import com.example.Angle.Services.Images.ImageUploadService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
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
import java.util.HashSet;
import java.util.Set;

@RestController
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200","http://142.93.104.248"})
@RequestMapping("")
public class AccountController {

    private final JwtService jwtService;
    private final AccountAdminService accountAdminService;

    private final AccountRetrievalService accountRetrievalService;
    private final ImageUploadService imageUploadService;
    private final ImageSaveService imageSaveService;

    private final ImageConverterService imageConverterService;

    private final MaintenanceMailsService maintenanceMailsService;

    private final PasswordEncoder passwordEncoder;

    private final UserRoleRepository userRoleRepository;

    private final UserAccountService userAccountService;

    private final AuthenticationManager authenticationManager;

    @Autowired
    public AccountController(JwtService jwtService, AccountAdminService accountAdminService,
                             AccountRetrievalService accountRetrievalService,
                             ImageUploadService imageUploadService,
                             ImageSaveService imageSaveService,
                             ImageConverterService imageConverterService,
                             MaintenanceMailsService maintenanceMailsService,
                             PasswordEncoder passwordEncoder,
                             UserRoleRepository userRoleRepository,
                             UserAccountService userAccountService,
                             AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.accountAdminService = accountAdminService;
        this.accountRetrievalService = accountRetrievalService;
        this.imageUploadService = imageUploadService;
        this.imageSaveService = imageSaveService;
        this.imageConverterService = imageConverterService;
        this.maintenanceMailsService = maintenanceMailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository;
        this.userAccountService = userAccountService;
        this.authenticationManager = authenticationManager;
    }

    private final Logger logger = LogManager.getLogger(AccountController.class);


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
        return accountRetrievalService.generateAccountResponse(accountRetrievalService.getCurrentUser().getId());
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

    @RequestMapping(value = "unAuth/login",method = RequestMethod.POST)
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

    @RequestMapping(value = "/unAuth/signup",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>signup(@RequestBody Account account) throws MediaNotFoundException {
        Set<UserRole> defaultRoles = new HashSet<>();
        defaultRoles.add(userRoleRepository.findByName("ROLE_USER").orElse(null));
        if(!defaultRoles.contains(null)){
            account.setRoles(defaultRoles);
        }else{
            logger.error("User Roles not found in a database! Please check the database!");
        }
        account.setConfirmed(false);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        accountAdminService.addUser(account);
        maintenanceMailsService.confirmationEmail(account.getEmail());
        return ResponseEntity.ok(new SimpleResponse("In order to login you need to confirm your email. Check your mailbox"));
    }

    @RequestMapping(value = "/unAuth/signup/checkEmail",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>checkEmail(@RequestBody String email) throws EmailExistsException {
        if(accountRetrievalService.emailExists(email)){
            throw new EmailExistsException();
        }else{
            return ResponseEntity.ok(new SimpleResponse("Email not found!"));
        }
    }

    @RequestMapping(value = "/unAuth/signup/confirmAccount",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>confirmAccount(@RequestParam String token) throws MediaNotFoundException, BadRequestException {
        Account account = accountRetrievalService.getUserByUsername(jwtService.extractUsername(token));
        if(jwtService.validateEmailConfirmationToken(token)){
            account.setConfirmed(true);
            accountAdminService.addUser(account);
            return ResponseEntity.ok(new SimpleResponse("Email has been confirmed! You're able to login now."));
        }
        maintenanceMailsService.confirmationEmail(account.getEmail());
        throw new BadRequestException("Confirmation timeout! New confirmation email has been sent!");
    }

    @RequestMapping(value = "/unAuth/signup/checkUsername",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>checkUsername(@RequestBody String username) throws UsernameExistsException {
        if(accountRetrievalService.usernameExists(username)){
            throw new UsernameExistsException();
        }else{
            return ResponseEntity.ok(new SimpleResponse("Username not found!"));
        }
    }

    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>logout(@RequestBody String token){
        this.jwtService.invalidateToken(token);
        return ResponseEntity.ok(new SimpleResponse("You've been logout!"));
    }


}
