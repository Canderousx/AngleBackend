package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Exceptions.FileStoreException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AccountRes;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.EnvironmentVariables;
import com.example.Angle.Models.Tag;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.*;
import com.example.Angle.Services.Images.ImageSaveService;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/auth/upload")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
public class UploadController {

    private final FileService fileService;

    private final TagService tagService;

    private final VideoRepository videoRepository;

    private final AccountRepository accountRepository;

    private final FFMpegService ffMpegService;

    private final EnvironmentVariables environmentVariables;

    private final ImageSaveService imageSaveService;

    private final Logger logger = LogManager.getLogger(UploadController.class);

    @Autowired
    public UploadController(FileService fileService,
                            TagService tagService,
                            VideoRepository videoRepository,
                            AccountRepository accountRepository,
                            FFMpegService ffMpegService,
                            EnvironmentVariables environmentVariables,
                            ImageSaveService imageSaveService){
        this.fileService = fileService;
        this.tagService = tagService;
        this.videoRepository = videoRepository;
        this.accountRepository = accountRepository;
        this.ffMpegService = ffMpegService;
        this.environmentVariables = environmentVariables;
        this.imageSaveService = imageSaveService;
    }

    @RequestMapping(value = "",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> uploadVideo(@RequestParam("file")MultipartFile file) throws FileStoreException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByUsername(username).orElse(null);
        Video video = new Video();
        video.setRawPath(this.fileService.storeFile(file));
        video.setDatePublished(new Date());
        video.setAuthorId(account.getId());
        videoRepository.save(video);
        video.setHlsPath(environmentVariables.getHlsOutputPath()+"\\"+video.getId()+"\\"+video.getId()+"_playlist.m3u8");
        videoRepository.save(video);
        try {
            CompletableFuture<Void> future = ffMpegService.convertToHls(video.getRawPath(), video.getId().toString());
            future.join();
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(new SimpleResponse("Unable to process the media!"));
        }
        return ResponseEntity.ok(new SimpleResponse(video.getId().toString()));
    }

    @RequestMapping(value = "/getThumbnails",method = RequestMethod.GET)
    public List<Thumbnail> getThumbnails(@RequestParam String v) throws IOException, InterruptedException, MediaNotFoundException {
        Video video = videoRepository.findById(v).orElse(null);
        Account account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElse(null);
        if(account == null){
            logger.error("You need to log in to edit video metadata!");
            throw new BadCredentialsException("You need to log in!");
        }
        if(video == null){
            logger.error("Media not found");
            throw new MediaNotFoundException("Couldn't find requested media!");
        }
        if(!account.getId().equals(video.getAuthorId())){
            logger.error("Unauthorized to edit media file ID: "+v);
            throw new BadCredentialsException("Unauthorized");
        }
        List<Thumbnail>generatedThumbs = ffMpegService.getVideoThumbnails(video.getRawPath());
        System.out.println("Received thumbnails: "+generatedThumbs.size());
        return generatedThumbs;
    }

    @RequestMapping(value = "/setMetadata",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>setMetadata(@RequestParam String id,
                                                     @RequestBody Video metadata) throws MediaNotFoundException {
        Video video = videoRepository.findById(id).orElse(null);
        if(video != null){
            video.setName(metadata.getName());
            video.setDescription(metadata.getDescription());
            Set<Tag> tags = new HashSet<>(metadata.getTags());
            video.setTags(tagService.setTags(metadata));
            try {
                video.setThumbnail(imageSaveService.saveVideoThumbnail(
                        metadata.getThumbnail(),
                        video.getId()
                ));
                videoRepository.save(video);
                return ResponseEntity.ok(new SimpleResponse("Your video has been saved!"));
            } catch (IOException e) {
                throw new MediaNotFoundException("There was an error during thumbnail processing");
            }

        }
        throw new MediaNotFoundException("Requested video not found");
    }
}
