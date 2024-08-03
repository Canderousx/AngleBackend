package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Services.Videos.VideoModerationService;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
@RequestMapping("/auth")
public class DataController {

    @Autowired
    private AccountRetrievalService accountRetrievalService;

    @Autowired
    AccountAdminService accountAdminService;

    @Autowired
    VideoModerationService videoModerationService;


    private final Logger logger = LogManager.getLogger(DataController.class);


    @RequestMapping(value = "/deleteVideo",method = RequestMethod.DELETE)
    public ResponseEntity<SimpleResponse>deleteVideo(@RequestParam String id) throws IOException, MediaNotFoundException, ClassNotFoundException, FileServiceException {
        videoModerationService.removeVideo(id);
        return ResponseEntity.ok(new SimpleResponse("Video has been removed!"));

    }


    @RequestMapping(value = "/checkRated",method = RequestMethod.GET)
    public List<Boolean> checkRated (@RequestParam String v) throws BadRequestException {
        List<Boolean> ratingData = new ArrayList<>();
        Account account = accountRetrievalService.getCurrentUser();
        if(account.getLikedVideos().contains(v) || account.getDislikedVideos().contains(v)){
            if(account.getLikedVideos().contains(v)){
                ratingData.add(true);
            }else{
                ratingData.add(false);
            }
        }
        return ratingData;
    }

    @RequestMapping(value = "/rateVideo",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> rateVideo(@RequestParam String v,
                                                    @RequestBody boolean rating) throws MediaNotFoundException, BadRequestException {
        Account account = accountRetrievalService.getCurrentUser();
            if(rating){
                if(!account.getLikedVideos().contains(v)){
                    this.videoModerationService.likeVideo(v);
                    if(account.getDislikedVideos().contains(v)){
                        logger.info("Video was disliked before... Removing dislike");
                        this.videoModerationService.removeDislike(v);
                        account.getDislikedVideos().remove(v);
                    }
                    account.getLikedVideos().add(v);
                    accountAdminService.addUser(account);
                }
            }
            if(!rating){
                if(!account.getDislikedVideos().contains(v)){
                    this.videoModerationService.dislikeVideo(v);
                    if(account.getLikedVideos().contains(v)){
                        logger.info("Video was liked before... Removing like");
                        this.videoModerationService.removeLike(v);
                        account.getLikedVideos().remove(v);
                    }
                    account.getDislikedVideos().add(v);
                    accountAdminService.addUser(account);
                }
            }
        return ResponseEntity.ok(new SimpleResponse("Operation ended successfully"));
    }
}
