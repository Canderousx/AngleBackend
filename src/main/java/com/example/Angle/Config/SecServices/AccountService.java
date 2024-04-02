package com.example.Angle.Config.SecServices;


import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final Logger log = LogManager.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;


    public void addUser(Account account){
        this.accountRepository.save(account);
    }

    public boolean banUser(UUID userId){
        Account account = this.accountRepository.findById(userId).orElse(null);
        if(account !=null){
            account.setActive(false);
            this.accountRepository.save(account);
            log.info("User ["+userId+"] has been banned");
            return true;
        }else{
            log.error("Requested user ["+userId+"] not FOUND!");
            return false;
        }
    }

    public boolean unbanUser(UUID userId){
        Account account = this.accountRepository.findById(userId).orElse(null);
        if(account !=null){
            account.setActive(true);
            this.accountRepository.save(account);
            log.info("User ["+userId+"] has been unbanned");
            return true;
        }else{
            log.error("Requested user ["+userId+"] not FOUND!");
            return false;
        }
    }

    public List<Account> getBannedUsers(){
        log.info("Sending Banned Users");
        return new ArrayList<>(this.accountRepository.findByActive(false).stream().toList());
    }

    public List<Account>getAllUsers(){
        List<Account> accounts = new ArrayList<>();
        this.accountRepository.findAll().forEach(accounts::add);
        log.info("Sending All Users ["+accounts.size()+"]");
        return accounts;
    }

    public Account getUser(UUID userId){
        return this.accountRepository.findById(userId).orElse(new Account());
    }

    public boolean usernameExists(String username){
        return this.accountRepository.findByUsername(username).isPresent();
    }

    public boolean emailExists(String email){

        return this.accountRepository.findByEmail(email).isPresent();
    }

    public boolean isActive(UUID userId){
        Account account = this.accountRepository.findById(userId).orElse(null);
        if(account!=null){
            return account.isActive();
        }
        return false;
    }



}
