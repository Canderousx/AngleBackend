package com.example.Angle.Controllers.Unauth;


import com.example.Angle.Config.Exceptions.EmailExistsException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Exceptions.UsernameExistsException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.UserRole;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.UserRoleRepository;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Config.SecServices.JwtService;
import com.example.Angle.Services.EmailService;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/unAuth/signup")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
public class Signup {

    private final Logger logger = LogManager.getLogger(Signup.class);

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    JwtService jwtService;


    @RequestMapping(value = "/confirmAccount",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>confirmAccount(@RequestParam String token) throws MediaNotFoundException, BadRequestException {
        Account account = accountService.getUserByUsername(jwtService.extractUsername(token));
        if(jwtService.validateEmailConfirmationToken(token)){
            account.setConfirmed(true);
            accountService.addUser(account);
            return ResponseEntity.ok(new SimpleResponse("Email has been confirmed! You're able to login now."));
        }
        emailService.confirmationEmail(account.getEmail());
        throw new BadRequestException("Confirmation timeout! New confirmation email has been sent!");
    }

    @RequestMapping(value = "/checkUsername",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>checkUsername(@RequestBody String username) throws UsernameExistsException {
        if(accountService.usernameExists(username)){
            System.out.println("Username exists!");
            throw new UsernameExistsException();
        }else{
            System.out.println("Username free!");
            return ResponseEntity.ok(new SimpleResponse("Username not found!"));
        }
    }

    @RequestMapping(value = "/checkEmail",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>checkEmail(@RequestBody String email) throws EmailExistsException {
        if(accountService.emailExists(email)){
            throw new EmailExistsException();
        }else{
            return ResponseEntity.ok(new SimpleResponse("Email not found!"));
        }
    }



    @RequestMapping(value = "",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>signup(@RequestBody Account account) throws MediaNotFoundException {
        Set<UserRole>defaultRoles = new HashSet<>();
        defaultRoles.add(userRoleRepository.findByName("ROLE_USER").orElse(null));
        if(!defaultRoles.contains(null)){
            account.setRoles(defaultRoles);
        }else{
            logger.error("User Roles not found in a database! Please check the database!");
        }
        account.setConfirmed(false);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        accountService.addUser(account);
        emailService.confirmationEmail(account.getEmail());
        return ResponseEntity.ok(new SimpleResponse("In order to login you need to confirm your email. Check your mailbox"));
    }

}
