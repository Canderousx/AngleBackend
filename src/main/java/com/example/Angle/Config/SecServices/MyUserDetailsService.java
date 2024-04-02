package com.example.Angle.Config.SecServices;

import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public Account loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Account>optAccount = accountRepository.findByEmail(email);
        if(optAccount.isPresent()){
            return optAccount.get();
        }else{
            throw new UsernameNotFoundException("Account does not exist!");
        }
    }
}
