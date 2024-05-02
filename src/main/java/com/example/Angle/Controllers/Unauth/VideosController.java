package com.example.Angle.Controllers.Unauth;


import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(value = "/unAuth/videos")
@CrossOrigin(value = {"http://localhost:4200"})
public class VideosController {

    private final int pageSize = 10;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    ImageService imageService;

    @Autowired
    AccountRepository accountRepository;

    @RequestMapping(value = "/getAll",method = RequestMethod.GET)
    public List<Video> getAllVideos(@RequestParam int page) throws IOException, ClassNotFoundException {
        Pageable paginateSettings = PageRequest.of(page,10, Sort.by("datePublished").descending());
        List<Video> videos = this.videoRepository.findAll(paginateSettings).stream().toList();
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

    @RequestMapping(value = "/getVideo",method = RequestMethod.GET)
    public Video getVideo(@RequestParam String id) throws IOException, ClassNotFoundException {
        Optional<Video> video = videoRepository.findById(UUID.fromString(id));
        return video.orElse(null);
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
