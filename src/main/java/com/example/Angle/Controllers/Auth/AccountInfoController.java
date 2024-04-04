package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AccountRes;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(value = {"http://localhost:4200"})
@RequestMapping("/auth")
public class AccountInfoController {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    private final Logger logger = LogManager.getLogger(AccountInfoController.class);


    @RequestMapping(value = "/getMyId",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse> getUserId(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByUsername(username).orElse(null);
        return ResponseEntity.ok(new SimpleResponse(account.getId().toString()));
    }

    @RequestMapping(value = "/getCurrentUser",method = RequestMethod.GET)
    public AccountRes getCurrentUser(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByUsername(username).orElse(null);
        return accountService.generateAccountResponse(account.getId());
    }

    @RequestMapping(value = "/getUserById",method = RequestMethod.GET)
    public AccountRes getUserById(@RequestParam String id){
        return accountService.generateAccountResponse(UUID.fromString(id));
    }
}
