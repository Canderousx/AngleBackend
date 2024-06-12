package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AccountRes;
import com.example.Angle.Config.Models.UserRole;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Config.SecServices.JwtService;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Services.ImageService;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@RestController
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
@RequestMapping("/auth")
public class AccountInfoController {


    @Autowired
    JwtService jwtService;

    @Autowired
    AccountService accountService;

    @Autowired
    ImageService imageService;

    private final Logger logger = LogManager.getLogger(AccountInfoController.class);

    @RequestMapping(value = "/isAdmin",method = RequestMethod.GET)
    public boolean isAdmin() throws BadRequestException {
        return accountService.isAdmin();
    }

    @RequestMapping(value = "/subscribe",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse>subscribe(@RequestParam String id) throws BadRequestException, MediaNotFoundException {
        Account user = accountService.getCurrentUser();
        Account channel = accountService.getUser(id);
        if(user == null || channel == null){
            throw new UsernameNotFoundException("Account or channel is null!");
        }
        if(user.getSubscribedIds().contains(id)){
            return ResponseEntity.ok(new SimpleResponse("Already subscribed!"));
        }
        user.getSubscribedIds().add(id);
        accountService.addUser(user);
        channel.getSubscribers().add(user.getId());
        accountService.addUser(channel);
        return ResponseEntity.ok(new SimpleResponse("Subscription success"));
    }

    @RequestMapping(value = "/unsubscribe",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse>unsubscribe(@RequestParam String id) throws BadRequestException, MediaNotFoundException {
        Account user = accountService.getCurrentUser();
        Account channel = accountService.getUser(id);
        if(user == null || channel == null){
            throw new UsernameNotFoundException("Account or channel is null!");
        }
        if(!user.getSubscribedIds().contains(id)){
            return ResponseEntity.ok(new SimpleResponse("Not a subscriber already!"));
        }
        user.getSubscribedIds().remove(id);
        accountService.addUser(user);
        channel.getSubscribers().remove(user.getId());
        accountService.addUser(channel);
        return ResponseEntity.ok(new SimpleResponse("Unsubscribed successfully"));
    }

    @RequestMapping(value = "/isSubscriber",method = RequestMethod.GET)
    public boolean isSubscriber(@RequestParam String id) throws BadRequestException {
        Account user = accountService.getCurrentUser();
        return user.getSubscribedIds().contains(id);
    }

    @RequestMapping(value = "/getMyId",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse> getUserId() throws BadRequestException {
        return ResponseEntity.ok(new SimpleResponse(accountService.getCurrentUser().getId()));
    }

    @RequestMapping(value = "/getCurrentUser",method = RequestMethod.GET)
    public AccountRes getCurrentUser() throws IOException, ClassNotFoundException {
        return accountService.generateAccountResponse(accountService.getCurrentUser().getId());
    }

    @RequestMapping(value = "/changeAvatar",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>changeAvatar(@RequestParam String id,
                                                      @RequestParam(name = "file") MultipartFile avatar) throws IOException {
        if(!imageService.checkExtension(avatar)){
            throw new InvalidFileNameException("","File extension not supported!");
        }
        Account account = accountService.getCurrentUser();
        account.setAvatar(imageService.saveUserAvatar(imageService.processAvatar(avatar), id));
        accountService.addUser(account);
        return ResponseEntity.ok(new SimpleResponse("Avatar has been changed successfully"));
    }

    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>logout(@RequestBody String token){
        this.jwtService.invalidateToken(token);
        return ResponseEntity.ok(new SimpleResponse("You've been logout!"));
    }
}
