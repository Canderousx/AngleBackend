package com.example.Angle.Controllers;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Services.Channels.ChannelService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class ChannelController {

    private final ChannelService channelService;

    @Autowired
    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @RequestMapping(value = "/subscribe",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse> subscribe(@RequestParam String id) throws BadRequestException, MediaNotFoundException {
        channelService.subscribe(id);
        return ResponseEntity.ok(new SimpleResponse("Subscribed"));
    }

    @RequestMapping(value = "/unsubscribe",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse>unsubscribe(@RequestParam String id) throws BadRequestException {
        channelService.unsubscribe(id);
        return ResponseEntity.ok(new SimpleResponse("Unsubscribed"));
    }

    @RequestMapping(value = "/isSubscriber",method = RequestMethod.GET)
    public boolean isSubscriber(@RequestParam String id) throws BadRequestException {
        return channelService.isSubscriber(id);
    }
}
