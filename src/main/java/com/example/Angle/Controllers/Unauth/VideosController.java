package com.example.Angle.Controllers.Unauth;


import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AccountRes;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.Comment;
import com.example.Angle.Models.Tag;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.CommentService;
import com.example.Angle.Services.CookieService;
import com.example.Angle.Services.ImageService;
import com.example.Angle.Services.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

@RestController
@RequestMapping(value = "/unAuth/videos")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
public class VideosController {

    private final int pageSize = 10;

    private final Logger logger = LogManager.getLogger(VideosController.class);

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    CookieService cookieService;

    @Autowired
    VideoService videoService;

    @Autowired
    ImageService imageService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CommentService commentService;

    @Autowired
    AccountService accountService;


    @RequestMapping(value = "/registerView",method = RequestMethod.PATCH)
    public ResponseEntity<SimpleResponse>registerView(@RequestParam String id){
            this.videoService.registerView(UUID.fromString(id));
            return ResponseEntity.ok(new SimpleResponse(""));
    }

    @RequestMapping(value = "/getSimilar",method = RequestMethod.GET)
    public List<Video>getSimilar(@RequestParam String id){
        return this.videoService.getSimilar(UUID.fromString(id));
    }


    private List<Comment>fillCommentData(List<Comment> comments) throws IOException, ClassNotFoundException {
        List<Comment>filled = new ArrayList<>();
        for(Comment comment: comments){
            Account account = accountRepository.findById(comment.getAuthorId()).orElse(null);
            if(account!=null){
                comment.setAuthorAvatar(imageService.readImage(account.getAvatar()).getContent());
                comment.setAuthorName(account.getUsername());
            }else{
                comment.setAuthorName("Account disabled");
            }
            filled.add(comment);
        }
        return filled;
    }



    @RequestMapping(value = "/addComment",method = RequestMethod.POST)
    public List<Comment> addComment(@RequestBody Comment comment,
                                    HttpServletResponse response) throws IOException, ClassNotFoundException {
        comment.setDatePublished(new Date());
        commentService.addComment(comment);
        Pageable paginateSettings = PageRequest.of(0,10,Sort.by("datePublished").descending());
        List<Comment> refreshed = fillCommentData(commentService.getVideoComments(comment.getVideoId(),paginateSettings));
        response.setHeader("totalComments",String.valueOf(refreshed.size()));
        return refreshed;
    }

    @RequestMapping(value = "/getComments",method = RequestMethod.GET)
    public List<Comment>getComments(@RequestParam String id,
                                    @RequestParam int page,
                                    HttpServletResponse httpResponse) throws IOException, ClassNotFoundException {
        Pageable paginateSettings = PageRequest.of(page,10,Sort.by("datePublished").descending());
        httpResponse.setHeader("totalComments", commentService.getTotalCommentsNum(UUID.fromString(id)));
        return fillCommentData(commentService.getVideoComments(UUID.fromString(id),paginateSettings));
    }

    @RequestMapping(value = "/getAll",method = RequestMethod.GET)
    public List<Video> getAllVideos(@RequestParam int page) throws IOException, ClassNotFoundException {
        Pageable paginateSettings = PageRequest.of(page,12, Sort.by("datePublished").descending());
        List<Video> videos = this.videoRepository.findAllByThumbnailIsNotNullAndNameIsNotNull(paginateSettings).stream().toList();
        for(Video video : videos){
            try {
                video.setThumbnail(imageService.readImage(video.getThumbnail()).getContent());
                Optional<Account> account = accountRepository.findById(video.getAuthorId());
                if (account.isPresent()) {
                    video.setAuthorAvatar(imageService.readImage(account.get().getAvatar()).getContent());
                }
            }catch (NullPointerException e){
                this.videoRepository.delete(video);
            }
        }
        return videos;
    }

    @RequestMapping(value = "/getUserById",method = RequestMethod.GET)
    public AccountRes getUserById(@RequestParam String id) throws IOException, ClassNotFoundException {
        return accountService.generateAccountResponse(UUID.fromString(id));
    }

    @RequestMapping(value = "/getVideo",method = RequestMethod.GET)
    public Video getVideo(@RequestParam String id) throws IOException, ClassNotFoundException {
        Optional<Video> video = videoRepository.findById(UUID.fromString(id));
        return video.orElse(null);
    }

    @RequestMapping(value = "/getMostPopular",method = RequestMethod.GET)
    public List<Video> getPopular(){
        List<Video> mostPopular = videoRepository.findMostPopular();
        mostPopular.forEach(video ->{
            try {
                video.setAuthorAvatar(imageService.readImage(
                        accountRepository.findById(video.getAuthorId())
                                .get().getAvatar()
                ).getContent());
                video.setThumbnail(imageService.readImage(video.getThumbnail()).getContent());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        return mostPopular;
    }

    @RequestMapping(value = "/addPreference",method = RequestMethod.GET)
    public void addPreferenceCookie(@RequestParam String videoId,
                                    HttpServletResponse response,
                                    HttpServletRequest request) throws JsonProcessingException {
        Set<Tag>preferences = cookieService.getCookieValue(request, "prefVideos", new TypeReference<Set<Tag>>() {
        });
        Video video = videoRepository.findById(UUID.fromString(videoId)).orElse(null);
        if(video == null){
            return;
        }
        if(preferences == null){
            logger.info("Preference cookie not found or couldn't be read!");
            preferences = new HashSet<>();
        }
        preferences.addAll(video.getTags());
        cookieService.setCookie(response,"prefVideos",preferences);
    }

    @RequestMapping(value = "/getRecommendedVideos",method = RequestMethod.GET)
    public List<Video> getRecommended(HttpServletRequest request) throws JsonProcessingException {
        List<UUID> preferences = cookieService.getCookieValue(request,"prefVideos",new TypeReference<List<UUID>>() {
        });
        if(preferences == null){
            return new ArrayList<>();
        }
        logger.info("Preferences read from cookie: "+preferences.size());


//        List<Video>recommended = videoRepository.findRecommended()
        return new ArrayList<>();

    }

    @RequestMapping(value = "/getUserVideos",method = RequestMethod.GET)
    public List<Video> getUserVideos(@RequestParam String id,
                                     @RequestParam int page,
                                     HttpServletResponse response) throws BadRequestException {
        Pageable pageable = PageRequest.of(page,pageSize,Sort.by("datePublished").descending());
        int totalVideos = videoRepository.findByAuthorId(UUID.fromString(id)).size();
        response.setHeader("totalVideos",String.valueOf(totalVideos));
        return videoService.getUserVideos(UUID.fromString(id),pageable);
    }

    @RequestMapping(value = "/getAccount",method = RequestMethod.GET)
    public Account getAccount(@RequestParam String id) throws IOException, ClassNotFoundException {
        Optional<Account> account = accountRepository.findById(UUID.fromString(id));
        if(account.isPresent()){
            account.get().setAvatar(imageService.readImage(
                    account.get().getAvatar()
            ).getContent());
            return account.get();
        }
        return null;
    }

    @RequestMapping(value = "/getUserAvatar", method = RequestMethod.GET)
    public Thumbnail getUserAvatar(@RequestParam String id) throws IOException, ClassNotFoundException {
        Optional<Account>account = accountRepository.findById(UUID.fromString(id));
        if(account.isEmpty()){
            throw new UsernameNotFoundException("No such user in db!");
        }
        return imageService.readImage(account.get().getAvatar());
    }


}
