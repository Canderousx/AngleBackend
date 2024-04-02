package com.example.Angle.Controllers.Unauthenticated;


import com.example.Angle.Config.Exceptions.EmailExistsException;
import com.example.Angle.Config.Exceptions.UsernameExistsException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AuthReq;
import com.example.Angle.Config.Models.AuthRes;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Config.SecServices.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/unAuth")
@CrossOrigin("http://localhost:4200")
public class Login {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;

//    @RequestMapping(value = "/signup", method = RequestMethod.POST)
//    public ResponseEntity<SimpleResponse> signup(@RequestBody Account account) throws Exception {
//        boolean emailExists = accountService.emailExists(account.getEmail());
//        boolean usernameExists = accountService.usernameExists(account.getUsername());
//        if(emailExists){
//            throw new EmailExistsException();
//        }
//        if(usernameExists){
//            throw new UsernameExistsException();
//        }
//        account.setPassword(passwordEncoder.encode(account.getPassword()));
//        accountService.addUser(account);
//        return ResponseEntity.ok(new SimpleResponse("Account has been created!"));
//    }

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public AuthRes login(@RequestBody AuthReq authReq,
                         HttpServletResponse response) throws IOException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authReq.getEmail(),authReq.getPassword())
        );
        if(authentication.isAuthenticated()){
            return AuthRes.builder()
                    .accessToken(jwtService.generateToken(authReq.getEmail()))
                    .build();
        }else{
            throw new BadCredentialsException("Incorrect username or password!");
        }

    }
}
