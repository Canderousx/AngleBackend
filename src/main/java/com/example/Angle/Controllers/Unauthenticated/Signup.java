package com.example.Angle.Controllers.Unauthenticated;


import com.example.Angle.Config.Exceptions.EmailExistsException;
import com.example.Angle.Config.Exceptions.UsernameExistsException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.AccountService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/unAuth/signup")
@CrossOrigin(value = {"http://localhost:4200"})
public class Signup {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;

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
    public ResponseEntity<SimpleResponse>signup(@RequestBody Account account){
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        accountService.addUser(account);
        return ResponseEntity.ok(new SimpleResponse("Success! You can now log into your Angle!"));
    }

}