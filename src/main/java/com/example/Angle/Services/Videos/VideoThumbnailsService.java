package com.example.Angle.Services.Videos;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.Video;
import com.example.Angle.Services.Images.ImageRetrievalService;
import com.example.Angle.Services.Videos.Interfaces.VideoThumbnailsInterface;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public class VideoThumbnailsService implements VideoThumbnailsInterface {

    private final AccountService accountService;

    private final ImageRetrievalService imageRetrievalService;

    public VideoThumbnailsService(AccountService accountService, ImageRetrievalService imageRetrievalService) {
        this.accountService = accountService;
        this.imageRetrievalService = imageRetrievalService;
    }

    @Override
    public void processThumbnail(Video video) throws IOException, ClassNotFoundException, MediaNotFoundException {
        video.setAuthorAvatar(
                accountService.generateAccountResponse(video.getAuthorId()).getAvatar()
        );
        video.setThumbnail(
                imageRetrievalService.getImage(
                        video.getThumbnail()
                ).getContent()
        );

    }

    @Override
    public void processThumbnails(List<Video> toProcess) {
        toProcess.forEach(video ->{
            try {
                processThumbnail(video);
            } catch (IOException | ClassNotFoundException | MediaNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

    }
}
