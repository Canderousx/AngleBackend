package com.example.Angle.Config.SecServices;


import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.UserRole;
import org.springframework.stereotype.Service;

@Service
public class UserRolesService {

    public boolean isAdmin(Account account){
        for(UserRole role : account.getRoles()){
            if(role.getName().equals("ROLE_ADMIN")){
                return true;
            }
        }
        return false;
    }
}
