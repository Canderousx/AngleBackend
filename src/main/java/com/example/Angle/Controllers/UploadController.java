package com.example.Angle.Controllers;


import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.FileStoreException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.FFMpeg.FFMpegDataRetrievalService;
import com.example.Angle.Services.Videos.VideoModerationService;
import com.example.Angle.Services.Videos.VideoUploadService;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/upload")
public class UploadController {
    private final FFMpegDataRetrievalService ffMpegDataRetrievalService;

    private final VideoUploadService videoUploadService;

    private final VideoModerationService videoModerationService;

    @Autowired
    public UploadController(VideoModerationService videoModerationService,
                            VideoUploadService videoUploadService,
                            FFMpegDataRetrievalService ffMpegDataRetrievalService
                            ){
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
    public List<Thumbnail> getThumbnails(@RequestParam String v) throws IOException, InterruptedException, MediaNotFoundException, ClassNotFoundException {
        return ffMpegDataRetrievalService.getVideoThumbnails(v);
    }

    @RequestMapping(value = "/setMetadata",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>setMetadata(@RequestParam String id,
                                                     @RequestBody Video metadata) throws MediaNotFoundException {
        this.videoModerationService.setMetadata(id,metadata);
        return ResponseEntity.ok(new SimpleResponse("Your video has been saved!"));
    }

}
