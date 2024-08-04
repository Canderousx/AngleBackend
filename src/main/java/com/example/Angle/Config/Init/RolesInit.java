package com.example.Angle.Config.Init;


import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.UserRole;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecRepositories.UserRoleRepository;
import com.example.Angle.Services.Email.MaintenanceMailsService;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RolesInit {

    private final Logger logger = LogManager.getLogger(RolesInit.class);

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostConstruct
    public void rolesDbInitialization(){
        if(userRoleRepository.findAll().isEmpty()){
            logger.info("Roles not found in a database. Performing initialization...");
            UserRole userRole = new UserRole();
            userRole.setName("ROLE_USER");
            userRoleRepository.save(userRole);
            userRole = new UserRole();
            userRole.setName("ROLE_ADMIN");
            userRoleRepository.save(userRole);
            logger.info("Roles Initialization Successful!");
        }else{
            logger.info("Roles found in a database!");
        }
        initalizeDefaultAdminAccount();
    }



    private void initalizeDefaultAdminAccount(){
        String defaultPassword = UUID.randomUUID().toString();
        Account account = accountRepository.findByUsername("Admin").orElse(null);
        if(account == null){
            account = new Account();
            account.setUsername("Admin");
            account.setEmail("admin@angle.com");

        }
        logger.info("**************************************");
        logger.info("DEFAULT ADMIN GENERATED PASSWORD: "+defaultPassword);
        logger.info("DEFAULT ADMIN EMAIL: "+account.getEmail());
        logger.info("KEEP IN MIND IT'S ONLY FOR DEV PURPOSES. DISABLE IT AT ONCE AND CREATE YOUR NORMAL ADMIN ACCOUNT!");
        logger.info("**************************************");
        account.setPassword(passwordEncoder.encode(defaultPassword));
        UserRole role = userRoleRepository.findByName("ROLE_ADMIN").orElse(null);
        account.getRoles().add(role);
        accountRepository.save(account);
    }





}
