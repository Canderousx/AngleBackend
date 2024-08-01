package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Config.SecServices.UserRolesService;
import com.example.Angle.Services.ReportService;
import com.example.Angle.Services.VideoService;
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
    private VideoService videoService;

    @Autowired
    private UserRolesService userRolesService;

    @Autowired
    private AccountService accountService;



    @Autowired
    private ReportService reportService;

    private final Logger logger = LogManager.getLogger(DataController.class);


    @RequestMapping(value = "/deleteVideo",method = RequestMethod.DELETE)
    public ResponseEntity<SimpleResponse>deleteVideo(@RequestParam String id) throws IOException, MediaNotFoundException, ClassNotFoundException, FileServiceException {
        videoService.removeVideo(id);
        return ResponseEntity.ok(new SimpleResponse("Video has been removed!"));

    }


    @RequestMapping(value = "/checkRated",method = RequestMethod.GET)
    public List<Boolean> checkRated (@RequestParam String v) throws BadRequestException {
        List<Boolean> ratingData = new ArrayList<>();
        Account account = accountService.getCurrentUser();
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
        Account account = accountService.getCurrentUser();
            if(rating){
                if(!account.getLikedVideos().contains(v)){
                    this.videoService.likeVideo(v);
                    if(account.getDislikedVideos().contains(v)){
                        logger.info("Video was disliked before... Removing dislike");
                        this.videoService.removeDislike(v);
                        account.getDislikedVideos().remove(v);
                    }
                    account.getLikedVideos().add(v);
                    accountService.addUser(account);
                }
            }
            if(!rating){
                if(!account.getDislikedVideos().contains(v)){
                    this.videoService.dislikeVideo(v);
                    if(account.getLikedVideos().contains(v)){
                        logger.info("Video was liked before... Removing like");
                        this.videoService.removeLike(v);
                        account.getLikedVideos().remove(v);
                    }
                    account.getDislikedVideos().add(v);
                    accountService.addUser(account);
                }
            }
        return ResponseEntity.ok(new SimpleResponse("Operation ended successfully"));
    }
}
