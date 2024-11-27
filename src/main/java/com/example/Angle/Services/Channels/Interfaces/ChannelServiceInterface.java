package com.example.Angle.Services.Channels.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import org.apache.coyote.BadRequestException;

public interface ChannelServiceInterface {

    void subscribe(String channelId) throws BadRequestException;

    void unsubscribe(String channelId) throws BadRequestException;

    boolean isSubscriber(String channelId) throws BadRequestException;
}
