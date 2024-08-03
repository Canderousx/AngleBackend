package com.example.Angle.Controllers.Auth;


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

    private final FileSaveService fileSaveService;

    private final TagSaverService tagSaverService;

    private final VideoRepository videoRepository;

    private final AccountRepository accountRepository;


    private final EnvironmentVariables environmentVariables;

    private final ImageSaveService imageSaveService;

    private final FFMpegDataRetrievalService ffMpegDataRetrievalService;

    private final FFMpegConverterService ffMpegConverterService;

    private final Logger logger = LogManager.getLogger(UploadController.class);

    @Autowired
    public UploadController(FileSaveService fileSaveService,
                            TagSaverService tagSaverService,
                            VideoRepository videoRepository,
                            AccountRepository accountRepository,
                            EnvironmentVariables environmentVariables,
                            ImageSaveService imageSaveService,
                            FFMpegDataRetrievalService ffMpegDataRetrievalService,
                            FFMpegConverterService ffMpegConverterService
                            ){
        this.fileSaveService = fileSaveService;
        this.tagSaverService = tagSaverService;
        this.videoRepository = videoRepository;
        this.accountRepository = accountRepository;
        this.environmentVariables = environmentVariables;
        this.imageSaveService = imageSaveService;
        this.ffMpegDataRetrievalService = ffMpegDataRetrievalService;
        this.ffMpegConverterService = ffMpegConverterService;
    }

    @RequestMapping(value = "",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> uploadVideo(@RequestParam("file")MultipartFile file) throws FileStoreException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByUsername(username).orElse(null);
        Video video = new Video();
        video.setRawPath(this.fileSaveService.saveRawFile(file));
        video.setDatePublished(new Date());
        video.setAuthorId(account.getId());
        videoRepository.save(video);
        video.setHlsPath(environmentVariables.getHlsOutputPath()+"\\"+video.getId()+"\\"+video.getId()+"_playlist.m3u8");
        videoRepository.save(video);
        try {
            CompletableFuture<Void> future = ffMpegConverterService.convertToHls(video.getRawPath(), video.getId());
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
        List<Thumbnail>generatedThumbs = ffMpegDataRetrievalService.getVideoThumbnails(video.getRawPath());
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
            video.setTags(tagSaverService.setTags(metadata));
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
