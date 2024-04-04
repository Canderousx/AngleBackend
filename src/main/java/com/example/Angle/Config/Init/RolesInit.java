package com.example.Angle.Config.Init;


import com.example.Angle.Config.Models.UserRole;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecRepositories.UserRoleRepository;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RolesInit {

    private final Logger logger = LogManager.getLogger(RolesInit.class);

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    AccountRepository accountRepository;

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
    }





}
