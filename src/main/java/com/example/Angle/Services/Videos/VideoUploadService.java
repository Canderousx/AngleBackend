package com.example.Angle.Services.Videos;


import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.FileStoreException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.EnvironmentVariables;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.FFMpeg.FFMpegConverterService;
import com.example.Angle.Services.Files.FileSaveService;
import com.example.Angle.Services.Videos.Interfaces.VideoUploadInterface;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Service
public class VideoUploadService implements VideoUploadInterface {

    private final AccountRetrievalService accountRetrievalService;

    private final FileSaveService fileSaveService;

    private final VideoRepository videoRepository;

    private final FFMpegConverterService ffMpegConverterService;

    private final EnvironmentVariables environmentVariables;

    private final Logger logger = LogManager.getLogger(VideoUploadService.class);



    @Autowired
    public VideoUploadService(AccountRetrievalService accountRetrievalService, FileSaveService fileSaveService, VideoRepository videoRepository, FFMpegConverterService ffMpegConverterService, EnvironmentVariables environmentVariables) {
        this.accountRetrievalService = accountRetrievalService;
        this.fileSaveService = fileSaveService;
        this.videoRepository = videoRepository;
        this.ffMpegConverterService = ffMpegConverterService;
        this.environmentVariables = environmentVariables;
    }

    @Override
    public void uploadVideo(MultipartFile file) throws BadRequestException, FileStoreException, FileServiceException {
        Account account = accountRetrievalService.getCurrentUser();
        Video video = new Video();
        video.setRawPath(this.fileSaveService.saveRawFile(file));
        video.setDatePublished(new Date());
        video.setAuthorId(account.getId());
        video.setProcessing(true);
        videoRepository.save(video);
        video.setHlsPath(environmentVariables.getHlsOutputPath()+File.pathSeparator+video.getId()+File.pathSeparator+video.getId()+"_playlist.m3u8");
        videoRepository.save(video);
        try {
            CompletableFuture<Void> future = ffMpegConverterService.convertToHls(video.getRawPath(), video.getId());
            future.join();
        }catch (Exception e){
            logger.error(e);
            throw new FileServiceException("Unable to process the media!");
        }
        video.setProcessing(false);
        videoRepository.save(video);
    }
}
