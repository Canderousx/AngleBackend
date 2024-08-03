package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AccountRes;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Config.SecServices.JwtService;
import com.example.Angle.Services.Images.ImageConverterService;
import com.example.Angle.Services.Images.ImageSaveService;
import com.example.Angle.Services.Images.ImageUploadService;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
@RequestMapping("/auth")
public class AccountController {

    private final JwtService jwtService;
    private final AccountAdminService accountAdminService;

    private final AccountRetrievalService accountRetrievalService;
    private final ImageUploadService imageUploadService;
    private final ImageSaveService imageSaveService;

    private final ImageConverterService imageConverterService;


    private final Logger logger = LogManager.getLogger(AccountController.class);

    @Autowired
    public AccountController(JwtService jwtService,
                             AccountAdminService accountAdminService,
                             ImageUploadService imageUploadService,
                             ImageSaveService imageSaveService,
                             ImageConverterService imageConverterService,
                             AccountRetrievalService accountRetrievalService){
        this.jwtService = jwtService;
        this.accountAdminService = accountAdminService;
        this.imageUploadService = imageUploadService;
        this.imageSaveService = imageSaveService;
        this.imageConverterService = imageConverterService;
        this.accountRetrievalService = accountRetrievalService;

    }

    @RequestMapping(value = "/isAdmin",method = RequestMethod.GET)
    public boolean isAdmin() throws BadRequestException {
        return accountRetrievalService.isAdmin();
    }

    @RequestMapping(value = "/subscribe",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse>subscribe(@RequestParam String id) throws BadRequestException, MediaNotFoundException {
        Account user = accountRetrievalService.getCurrentUser();
        Account channel = accountRetrievalService.getUser(id);
        if(user == null || channel == null){
            throw new UsernameNotFoundException("Account or channel is null!");
        }
        if(user.getSubscribedIds().contains(id)){
            return ResponseEntity.ok(new SimpleResponse("Already subscribed!"));
        }
        user.getSubscribedIds().add(id);
        accountAdminService.addUser(user);
        channel.getSubscribers().add(user.getId());
        accountAdminService.addUser(channel);
        return ResponseEntity.ok(new SimpleResponse("Subscription success"));
    }

    @RequestMapping(value = "/unsubscribe",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse>unsubscribe(@RequestParam String id) throws BadRequestException, MediaNotFoundException {
        Account user = accountRetrievalService.getCurrentUser();
        Account channel = accountRetrievalService.getUser(id);
        if(user == null || channel == null){
            throw new UsernameNotFoundException("Account or channel is null!");
        }
        if(!user.getSubscribedIds().contains(id)){
            return ResponseEntity.ok(new SimpleResponse("Not a subscriber already!"));
        }
        user.getSubscribedIds().remove(id);
        accountAdminService.addUser(user);
        channel.getSubscribers().remove(user.getId());
        accountAdminService.addUser(channel);
        return ResponseEntity.ok(new SimpleResponse("Unsubscribed successfully"));
    }

    @RequestMapping(value = "/isSubscriber",method = RequestMethod.GET)
    public boolean isSubscriber(@RequestParam String id) throws BadRequestException {
        Account user = accountRetrievalService.getCurrentUser();
        return user.getSubscribedIds().contains(id);
    }

    @RequestMapping(value = "/getMyId",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse> getUserId() throws BadRequestException {
        return ResponseEntity.ok(new SimpleResponse(accountRetrievalService.getCurrentUser().getId()));
    }

    @RequestMapping(value = "/getCurrentUser",method = RequestMethod.GET)
    public AccountRes getCurrentUser() throws IOException, ClassNotFoundException {
        return accountRetrievalService.generateAccountResponse(accountRetrievalService.getCurrentUser().getId());
    }

    @RequestMapping(value = "/changeAvatar",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>changeAvatar(@RequestParam String id,
                                                      @RequestParam(name = "file") MultipartFile avatar) throws IOException {
        if(!imageUploadService.checkExtension(avatar)){
            throw new InvalidFileNameException("","File extension not supported!");
        }
        Account account = accountRetrievalService.getCurrentUser();
        account.setAvatar(imageSaveService.saveUserAvatar(imageConverterService.convertAvatarToBase64(avatar), id));
        accountAdminService.addUser(account);
        return ResponseEntity.ok(new SimpleResponse("Avatar has been changed successfully"));
    }

    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>logout(@RequestBody String token){
        this.jwtService.invalidateToken(token);
        return ResponseEntity.ok(new SimpleResponse("You've been logout!"));
    }
}
