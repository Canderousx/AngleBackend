package com.example.Angle.Controllers;


import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.FileStoreException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.Models.EnvironmentVariables;
import com.example.Angle.Models.Tag;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.FFMpeg.FFMpegConverterService;
import com.example.Angle.Services.FFMpeg.FFMpegDataRetrievalService;
import com.example.Angle.Services.Files.FileSaveService;
import com.example.Angle.Services.Images.ImageSaveService;
import com.example.Angle.Services.Tags.TagSaverService;
import com.example.Angle.Services.Videos.VideoModerationService;
import com.example.Angle.Services.Videos.VideoUploadService;
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
@RequestMapping("/upload")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200","http://142.93.104.248"})
public class UploadController {

    private final VideoRepository videoRepository;

    private final AccountRepository accountRepository;


    private final FFMpegDataRetrievalService ffMpegDataRetrievalService;

    private final VideoUploadService videoUploadService;

    private final VideoModerationService videoModerationService;


    private final Logger logger = LogManager.getLogger(UploadController.class);

    @Autowired
    public UploadController(VideoModerationService videoModerationService,
                            VideoRepository videoRepository,
                            AccountRepository accountRepository,
                            VideoUploadService videoUploadService,
                            FFMpegDataRetrievalService ffMpegDataRetrievalService
                            ){
        this.videoRepository = videoRepository;
        this.accountRepository = accountRepository;
        this.videoModerationService = videoModerationService;
        this.ffMpegDataRetrievalService = ffMpegDataRetrievalService;
        this.videoUploadService = videoUploadService;
    }

    @RequestMapping(value = "",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> uploadVideo(@RequestParam("file")MultipartFile file) throws FileStoreException, FileServiceException, BadRequestException {
        this.videoUploadService.uploadVideo(file);
        return ResponseEntity.ok(new SimpleResponse("Video has been uploaded and is being processed"));
    }

    @RequestMapping(value = "/getThumbnails",method = RequestMethod.GET)
    public List<Thumbnail> getThumbnails(@RequestParam String v) throws IOException, InterruptedException, MediaNotFoundException {
        Video video = videoRepository.findById(v).orElse(null);
        Account account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElse(null);
        if(account == null){
            throw new BadCredentialsException("You need to log in!");
        }
        if(video == null){
            throw new MediaNotFoundException("Couldn't find requested media!");
        }
        if(!account.getId().equals(video.getAuthorId())){
            throw new BadCredentialsException("Unauthorized");
        }
        List<Thumbnail>generatedThumbs = ffMpegDataRetrievalService.getVideoThumbnails(video.getRawPath());
        System.out.println("Received thumbnails: "+generatedThumbs.size());
        return generatedThumbs;
    }

    @RequestMapping(value = "/setMetadata",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>setMetadata(@RequestParam String id,
                                                     @RequestBody Video metadata) throws MediaNotFoundException {
        this.videoModerationService.setMetadata(id,metadata);
        return ResponseEntity.ok(new SimpleResponse("Your video has been saved!"));
    }

}
