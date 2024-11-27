package com.example.Angle.Controllers;


import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Config.SecServices.Account.Interfaces.AccountRetrievalServiceInterface;
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
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping(value = "")
public class VideoController {

    private final Logger logger = LogManager.getLogger(VideoController.class);

    private final VideoRetrievalService videoRetrievalService;

    private final VideoModerationService videoModerationService;


    private final CommentRetrievalService commentRetrievalService;

    private final AccountRetrievalService accountRetrievalService;

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
    }

    @RequestMapping(value = "/deleteVideo",method = RequestMethod.DELETE)
    public ResponseEntity<SimpleResponse>deleteVideo(@RequestParam String id) throws IOException, MediaNotFoundException, ClassNotFoundException, FileServiceException {
        videoModerationService.removeVideo(id);
        return ResponseEntity.ok(new SimpleResponse("Video has been removed!"));

    }

    @RequestMapping(value = "/checkRated",method = RequestMethod.GET)
    public int checkRated (@RequestParam String v) throws BadRequestException {
        // 0 means non-rated, 1 is liked, 2 is disliked
        return videoRetrievalService.checkRated(v);
    }

    @RequestMapping(value = "/rateVideo",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> rateVideo(@RequestParam String v,
                                                    @RequestBody boolean rating) throws MediaNotFoundException, BadRequestException {
        videoModerationService.rateVideo(v,rating);
        return ResponseEntity.ok(new SimpleResponse("Video rated"));
    }


    @RequestMapping(value = "/unAuth/videos/registerView",method = RequestMethod.PATCH)
    public ResponseEntity<String>registerView(@RequestParam String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
            this.videoModerationService.registerView(id);
            return ResponseEntity.ok("");
    }

    @RequestMapping(value = "/unAuth/videos/getSimilar",method = RequestMethod.GET)
    public List<Video>getSimilar(@RequestParam String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
        return this.videoRetrievalService.getSimilar(id);
    }

    @RequestMapping(value = "/unAuth/videos/getBySubscribers")
    public Page<Video>getBySubscribers(@RequestParam String page) throws BadRequestException {
        return videoRetrievalService.getRandomBySubscribers(Integer.parseInt(page));
    }

    @RequestMapping(value = "/unAuth/videos/getComments",method = RequestMethod.GET)
    public Page<Comment> getComments(@RequestParam String id,
                                     @RequestParam int page,
                                     @RequestParam int pageSize,
                                     HttpServletResponse httpResponse) throws IOException, ClassNotFoundException, MediaNotFoundException {
        Page<Comment> comments = commentRetrievalService.getVideoComments(id,page,pageSize);
        httpResponse.setHeader("totalComments", String.valueOf(comments.getTotalElements()));
        return comments;
    }

    @RequestMapping(value = "/unAuth/videos/getAll",method = RequestMethod.GET)
    public Page<Video> getAllVideos(@RequestParam int page) throws IOException, ClassNotFoundException {
        return videoRetrievalService.getAllVideos(page);
    }

    @RequestMapping(value = "/unAuth/videos/getUserById",method = RequestMethod.GET)
    public AccountRetrievalServiceInterface.AccountRecord getUserById(@RequestParam String id) throws IOException, ClassNotFoundException {
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
    public Page<Video> getUserVideos(@RequestParam String id,
                                     @RequestParam int page,
                                     @RequestParam int pageSize,
                                     HttpServletResponse response) throws BadRequestException, MediaNotFoundException {
        Page<Video> userVideos = videoRetrievalService.getUserVideos(id,page,pageSize);
        response.setHeader("totalVideos",String.valueOf(userVideos.getTotalElements()));
        return userVideos;
    }

    @RequestMapping(value = "/unAuth/videos/getUserAvatar", method = RequestMethod.GET)
    public Thumbnail getUserAvatar(@RequestParam String id) throws IOException, ClassNotFoundException {
        return new Thumbnail(accountRetrievalService.generateAccountResponse(id).avatar());
    }


}
