package com.example.Angle.Controllers.Unauth;


import com.example.Angle.Config.Models.AuthReq;
import com.example.Angle.Config.Models.AuthRes;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Config.SecServices.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/unAuth")
@CrossOrigin("http://localhost:4200")
public class Login {

    private final Logger logger = LogManager.getLogger(Login.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public AuthRes login(@RequestBody AuthReq authReq,
                         HttpServletResponse response) throws IOException {
        if(!accountService.emailExists(authReq.getEmail())){
            throw new BadCredentialsException("Incorrect username or password!");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authReq.getEmail(),authReq.getPassword())
        );
        if(authentication.isAuthenticated()){
            logger.info("Generating TOKEN");
            return AuthRes.builder()
                    .authToken(jwtService.generateToken(authReq.getEmail()))
                    .build();
        }else{
            throw new BadCredentialsException("Incorrect username or password!");
        }

    }
}
