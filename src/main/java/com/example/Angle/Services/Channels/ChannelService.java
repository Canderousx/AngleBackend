package com.example.Angle.Services.Channels;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Services.Channels.Interfaces.ChannelServiceInterface;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ChannelService implements ChannelServiceInterface {

    private final AccountRetrievalService accountRetrievalService;

    private final AccountAdminService accountAdminService;

    @Autowired
    public ChannelService(AccountRetrievalService accountRetrievalService, AccountAdminService accountAdminService) {
        this.accountRetrievalService = accountRetrievalService;
        this.accountAdminService = accountAdminService;
    }

    @Override
    public void subscribe(String channelId) throws BadRequestException {
        Account user = accountRetrievalService.getCurrentUser();
        Account channel = accountRetrievalService.getUser(channelId);
        if(user == null || channel == null){
            throw new RuntimeException("Account or channel is null!");
        }
        if(!user.getSubscribedIds().contains(channelId)){
            user.getSubscribedIds().add(channelId);
            accountAdminService.addUser(user);
            channel.getSubscribers().add(user.getId());
            accountAdminService.addUser(channel);
        }
    }

    @Override
    public void unsubscribe(String channelId) throws BadRequestException {
        Account user = accountRetrievalService.getCurrentUser();
        Account channel = accountRetrievalService.getUser(channelId);
        if(user == null || channel == null){
            throw new RuntimeException("Account or channel is null!");
        }
        if(user.getSubscribedIds().contains(channelId)){
            user.getSubscribedIds().remove(channelId);
            accountAdminService.addUser(user);
            channel.getSubscribers().remove(user.getId());
            accountAdminService.addUser(channel);
        }
    }

    @Override
    public boolean isSubscriber(String channelId) throws BadRequestException {
        Account user = accountRetrievalService.getCurrentUser();
        return user.getSubscribedIds().contains(channelId);
    }
}
