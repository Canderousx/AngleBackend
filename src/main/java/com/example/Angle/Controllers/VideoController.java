package com.example.Angle.Controllers;


import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AccountRes;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Models.Comment;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Models.Video;
import com.example.Angle.Services.Comments.CommentRetrievalService;
import com.example.Angle.Services.Videos.VideoModerationService;
import com.example.Angle.Services.Videos.VideoRetrievalService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping(value = "")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
public class VideoController {

    private final Logger logger = LogManager.getLogger(VideoController.class);

    private final VideoRetrievalService videoRetrievalService;

    private final VideoModerationService videoModerationService;


    private final CommentRetrievalService commentRetrievalService;

    private final AccountRetrievalService accountRetrievalService;

    private final AccountAdminService accountAdminService;

    @Autowired
    public VideoController(VideoRetrievalService videoRetrievalService,
                           VideoModerationService videoModerationService,
                           CommentRetrievalService commentRetrievalService,
                           AccountRetrievalService accountRetrievalService,
                           AccountAdminService accountAdminService) {
        this.videoRetrievalService = videoRetrievalService;
        this.videoModerationService = videoModerationService;
        this.commentRetrievalService = commentRetrievalService;
        this.accountRetrievalService = accountRetrievalService;
        this.accountAdminService = accountAdminService;
    }

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


    @RequestMapping(value = "/unAuth/videos/registerView",method = RequestMethod.PATCH)
    public ResponseEntity<SimpleResponse>registerView(@RequestParam String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
            this.videoModerationService.registerView(id);
            return ResponseEntity.ok(new SimpleResponse("View registered!"));
    }

    @RequestMapping(value = "/unAuth/videos/getSimilar",method = RequestMethod.GET)
    public List<Video>getSimilar(@RequestParam String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
        return this.videoRetrievalService.getSimilar(id);
    }

    @RequestMapping(value = "/unAuth/videos/getBySubscribers")
    public List<Video>getBySubscribers(@RequestParam String page) throws BadRequestException {
        return videoRetrievalService.getRandomBySubscribers(Integer.parseInt(page));
    }

    @RequestMapping(value = "/unAuth/videos/getComments",method = RequestMethod.GET)
    public List<Comment>getComments(@RequestParam String id,
                                    @RequestParam int page,
                                    @RequestParam int pageSize,
                                    HttpServletResponse httpResponse) throws IOException, ClassNotFoundException, MediaNotFoundException {
        Pageable paginateSettings = PageRequest.of(page,pageSize,Sort.by("datePublished").descending());
        httpResponse.setHeader("totalComments", String.valueOf(commentRetrievalService.getTotalCommentsNum(id)));
        return commentRetrievalService.getVideoComments(id,paginateSettings);
    }

    @RequestMapping(value = "/unAuth/videos/getAll",method = RequestMethod.GET)
    public List<Video> getAllVideos(@RequestParam int page) throws IOException, ClassNotFoundException {
        return videoRetrievalService.getAllVideos(page);
    }

    @RequestMapping(value = "/unAuth/videos/getUserById",method = RequestMethod.GET)
    public AccountRes getUserById(@RequestParam String id) throws IOException, ClassNotFoundException {
        return accountRetrievalService.generateAccountResponse(id);
    }

    @RequestMapping(value = "/unAuth/videos/getVideo",method = RequestMethod.GET)
    public Video getVideo(@RequestParam String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
        return videoRetrievalService.getVideo(id);
    }

    @RequestMapping(value = "/unAuth/videos/getMostPopular",method = RequestMethod.GET)
    public List<Video> getPopular(){
        return videoRetrievalService.getMostPopular();
    }

    @RequestMapping(value = "/unAuth/videos/getUserVideos",method = RequestMethod.GET)
    public List<Video> getUserVideos(@RequestParam String id,
                                     @RequestParam int page,
                                     @RequestParam int pageSize,
                                     HttpServletResponse response) throws BadRequestException, MediaNotFoundException {
        Pageable pageable = PageRequest.of(page,pageSize,Sort.by("datePublished").descending());
        int totalVideos = videoRetrievalService.howManyUserVideos(id);
        response.setHeader("totalVideos",String.valueOf(totalVideos));
        return videoRetrievalService.getUserVideos(id,pageable);
    }

    @RequestMapping(value = "/unAuth/videos/getUserAvatar", method = RequestMethod.GET)
    public Thumbnail getUserAvatar(@RequestParam String id) throws IOException, ClassNotFoundException {
        return new Thumbnail(accountRetrievalService.generateAccountResponse(id).getAvatar());
    }


}
