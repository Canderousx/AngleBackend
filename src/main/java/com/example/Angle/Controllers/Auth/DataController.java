package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.UserRolesService;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.VideoService;
import jakarta.annotation.PostConstruct;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
@RequestMapping("/auth")
public class DataController {


    @Autowired
    VideoRepository videoRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    VideoService videoService;

    @Autowired
    UserRolesService userRolesService;

    private final Logger logger = LogManager.getLogger(DataController.class);


    @RequestMapping(value = "/deleteVideo",method = RequestMethod.DELETE)
    public ResponseEntity<SimpleResponse>deleteVideo(@RequestParam String id) throws BadRequestException, MediaNotFoundException {
        Account account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication()
                .getName()).orElse(null);

        if(account == null){
            throw new BadRequestException();
        }
        Video video = videoRepository.findById(UUID.fromString(id)).orElse(null);
        if(video == null){
            throw new MediaNotFoundException("Requested media doesn't exist");
        }
        if(account.getId().equals(video.getAuthorId()) || userRolesService.isAdmin(account)){

            if(videoService.removeVideo(video)){
                return ResponseEntity.ok(new SimpleResponse("Video has been removed!"));
            }else{
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SimpleResponse("Please check server logs"));
            }
        }else{
            throw new BadRequestException();
        }

    }


    @RequestMapping(value = "/checkRated",method = RequestMethod.GET)
    public List<Boolean> checkRated (@RequestParam String v){
        List<Boolean> ratingData = new ArrayList<>();
        UUID id = UUID.fromString(v);
        Account account = accountRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElse(null);
        if(account!=null && (account.getLikedVideos().contains(id) || account.getDislikedVideos().contains(id))){
            if(account.getLikedVideos().contains(id)){
                ratingData.add(true);
            }else{
                ratingData.add(false);
            }
        }
        return ratingData;
    }

    @RequestMapping(value = "/rateVideo",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> rateVideo(@RequestParam String v,
                                                    @RequestBody boolean rating){
        Account account = accountRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElse(null);
        if(account!=null){
            UUID id = UUID.fromString(v);
            if(rating){
                if(!account.getLikedVideos().contains(id)){
                    this.videoService.likeVideo(id);
                    if(account.getDislikedVideos().contains(id)){
                        logger.info("Video was disliked before... Removing dislike");
                        this.videoService.removeDislike(id);
                        account.getDislikedVideos().remove(id);
                    }
                    account.getLikedVideos().add(id);
                    accountRepository.save(account);
                }
            }
            if(!rating){
                if(!account.getDislikedVideos().contains(id)){
                    this.videoService.dislikeVideo(id);
                    if(account.getLikedVideos().contains(id)){
                        logger.info("Video was liked before... Removing like");
                        this.videoService.removeLike(id);
                        account.getLikedVideos().remove(id);
                    }
                    account.getDislikedVideos().add(id);
                    accountRepository.save(account);
                }
            }

        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new SimpleResponse("You need to log in to rate a video!"));
        }
        return ResponseEntity.ok(new SimpleResponse("Operation ended successfully"));
    }
}
