package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AccountRes;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Services.ImageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
@RequestMapping("/auth")
public class AccountInfoController {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ImageService imageService;

    private final Logger logger = LogManager.getLogger(AccountInfoController.class);


    @RequestMapping(value = "/getMyId",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse> getUserId(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByUsername(username).orElse(null);
        return ResponseEntity.ok(new SimpleResponse(account.getId().toString()));
    }

    @RequestMapping(value = "/getCurrentUser",method = RequestMethod.GET)
    public AccountRes getCurrentUser() throws IOException, ClassNotFoundException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByUsername(username).orElse(null);
        if(account == null){
            throw new UsernameNotFoundException("User doesnt exists");
        }
        AccountRes response = accountService.generateAccountResponse(account.getId());
        return response;
    }



    @RequestMapping(value = "/changeAvatar",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>changeAvatar(@RequestParam String id,
                                                      @RequestParam(name = "file") MultipartFile avatar) throws IOException {
        Account account = accountRepository.findById(UUID.fromString(id)).orElse(null);
        if(account == null){
            throw new UsernameNotFoundException("User doesn't exist");
        }
        account.setAvatar(imageService.saveUserAvatar(imageService.imageToBase64(avatar), id));
        accountRepository.save(account);
        return ResponseEntity.ok(new SimpleResponse("Avatar has been changed successfully"));
    }
}
