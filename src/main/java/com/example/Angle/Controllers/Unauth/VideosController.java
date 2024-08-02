package com.example.Angle.Controllers.Unauth;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.AccountRes;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.AccountService;
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
@RequestMapping(value = "/unAuth/videos")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
public class VideosController {

    private final Logger logger = LogManager.getLogger(VideosController.class);


    @Autowired
    VideoRetrievalService videoRetrievalService;

    @Autowired
    VideoModerationService videoModerationService;


    @Autowired
    CommentRetrievalService commentRetrievalService;

    @Autowired
    AccountService accountService;


    @RequestMapping(value = "/registerView",method = RequestMethod.PATCH)
    public ResponseEntity<SimpleResponse>registerView(@RequestParam String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
            this.videoModerationService.registerView(id);
            return ResponseEntity.ok(new SimpleResponse("View registered!"));
    }

    @RequestMapping(value = "/getSimilar",method = RequestMethod.GET)
    public List<Video>getSimilar(@RequestParam String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
        return this.videoRetrievalService.getSimilar(id);
    }




    @RequestMapping(value = "/getBySubscribers")
    public List<Video>getBySubscribers(@RequestParam String page) throws BadRequestException {
        return videoRetrievalService.getRandomBySubscribers(Integer.parseInt(page));
    }





    @RequestMapping(value = "/getComments",method = RequestMethod.GET)
    public List<Comment>getComments(@RequestParam String id,
                                    @RequestParam int page,
                                    @RequestParam int pageSize,
                                    HttpServletResponse httpResponse) throws IOException, ClassNotFoundException, MediaNotFoundException {
        Pageable paginateSettings = PageRequest.of(page,pageSize,Sort.by("datePublished").descending());
        httpResponse.setHeader("totalComments", String.valueOf(commentRetrievalService.getTotalCommentsNum(id)));
        return commentRetrievalService.getVideoComments(id,paginateSettings);
    }


    @RequestMapping(value = "/getAll",method = RequestMethod.GET)
    public List<Video> getAllVideos(@RequestParam int page) throws IOException, ClassNotFoundException {
        return videoRetrievalService.getAllVideos(page);
    }

    @RequestMapping(value = "/getUserById",method = RequestMethod.GET)
    public AccountRes getUserById(@RequestParam String id) throws IOException, ClassNotFoundException {
        return accountService.generateAccountResponse(id);
    }

    @RequestMapping(value = "/getVideo",method = RequestMethod.GET)
    public Video getVideo(@RequestParam String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
        return videoRetrievalService.getVideo(id);
    }

    @RequestMapping(value = "/getMostPopular",method = RequestMethod.GET)
    public List<Video> getPopular(){
        return videoRetrievalService.getMostPopular();
    }

    @RequestMapping(value = "/getUserVideos",method = RequestMethod.GET)
    public List<Video> getUserVideos(@RequestParam String id,
                                     @RequestParam int page,
                                     @RequestParam int pageSize,
                                     HttpServletResponse response) throws BadRequestException, MediaNotFoundException {
        Pageable pageable = PageRequest.of(page,pageSize,Sort.by("datePublished").descending());
        int totalVideos = videoRetrievalService.howManyUserVideos(id);
        response.setHeader("totalVideos",String.valueOf(totalVideos));
        return videoRetrievalService.getUserVideos(id,pageable);
    }

    @RequestMapping(value = "/getAccount",method = RequestMethod.GET)
    public AccountRes getAccount(@RequestParam String id) throws IOException, ClassNotFoundException {
        return accountService.generateAccountResponse(id);
    }

    @RequestMapping(value = "/getUserAvatar", method = RequestMethod.GET)
    public Thumbnail getUserAvatar(@RequestParam String id) throws IOException, ClassNotFoundException {
        return new Thumbnail(accountService.generateAccountResponse(id).getAvatar());
    }


}
