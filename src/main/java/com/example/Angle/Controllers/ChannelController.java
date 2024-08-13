package com.example.Angle.Controllers;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200","http://142.93.104.248"})
@RequestMapping("")
public class ChannelController {

    private final AccountRetrievalService accountRetrievalService;

    private final AccountAdminService accountAdminService;

    @Autowired
    public ChannelController(AccountRetrievalService accountRetrievalService, AccountAdminService accountAdminService) {
        this.accountRetrievalService = accountRetrievalService;
        this.accountAdminService = accountAdminService;
    }

    @RequestMapping(value = "/subscribe",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse> subscribe(@RequestParam String id) throws BadRequestException, MediaNotFoundException {
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
}
