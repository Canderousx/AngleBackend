package com.example.Angle.Services.Videos;

import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Models.Tag;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.Comments.CommentManagementService;
import com.example.Angle.Services.Files.FileDeleterService;
import com.example.Angle.Services.Images.ImageSaveService;
import com.example.Angle.Services.Tags.TagSaverService;
import com.example.Angle.Services.Videos.Interfaces.VideoModerationInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


@Service
public class VideoModerationService implements VideoModerationInterface {

    private final Logger log = LogManager.getLogger(VideoModerationService.class);

    private final VideoRetrievalService videoRetrievalService;

    private final VideoRepository videoRepository;

    private final FileDeleterService fileDeleterService;

    private final CommentManagementService commentManagementService;

    private final AccountAdminService accountAdminService;

    private final TagSaverService tagSaverService;

    private final ImageSaveService imageSaveService;



    @Autowired
    public VideoModerationService(VideoRetrievalService videoRetrievalService,
                                  VideoRepository videoRepository,
                                  FileDeleterService fileDeleterService,
                                  CommentManagementService commentManagementService,
                                  AccountAdminService accountAdminService,
                                  TagSaverService tagSaverService,
                                  ImageSaveService imageSaveService) {
        this.videoRetrievalService = videoRetrievalService;
        this.videoRepository = videoRepository;
        this.fileDeleterService = fileDeleterService;
        this.commentManagementService = commentManagementService;
        this.accountAdminService = accountAdminService;
        this.tagSaverService = tagSaverService;
        this.imageSaveService = imageSaveService;
    }

    @Override
    public void setMetadata(String id, Video metadata) throws MediaNotFoundException {
        Video video = videoRepository.findById(id).orElse(null);
        if(video != null){
            video.setName(metadata.getName());
            video.setDescription(metadata.getDescription());
            video.setTags(tagSaverService.setTags(metadata));
            try {
                video.setThumbnail(imageSaveService.saveVideoThumbnail(
                        metadata.getThumbnail(),
                        video.getId()
                ));
                videoRepository.save(video);
            } catch (IOException e) {
                throw new MediaNotFoundException("There was an error during thumbnail processing");
            }

        }
        throw new MediaNotFoundException("Requested video not found");
    }

    @Override
    public void registerView(String videoId) throws MediaNotFoundException {
        Video video = videoRetrievalService.getRawVideo(videoId);
        video.setViews(video.getViews()+1);
        videoRepository.save(video);
    }

    @Override
    public void removeVideo(String id) throws MediaNotFoundException, FileServiceException {
        Video video = videoRetrievalService.getRawVideo(id);
        fileDeleterService.deleteVideoFiles(video);
        log.info("Files removed successfully! Removing database entries");
        commentManagementService.removeVideoComments(video.getId());
        accountAdminService.removeLikeInteractions(video.getId());
        accountAdminService.removeDislikeInteractions(video.getId());
        videoRepository.deleteTagAssociations(video.getId());
        videoRepository.delete(video);
        log.info("DB cleaned.");

    }

    @Override
    public void banVideo(String videoId) throws MediaNotFoundException {
        Video toBan = videoRetrievalService.getRawVideo(videoId);
        toBan.setBanned(true);
        this.videoRepository.save(toBan);
        log.info("Video has been banned: "+toBan.getId());
    }

    @Override
    public void unbanVideo(String videoId) throws MediaNotFoundException {
        Video toUnban = videoRetrievalService.getRawVideo(videoId);
        toUnban.setBanned(false);
        this.videoRepository.save(toUnban);
        log.info("Video has been unbanned: "+toUnban.getId());
    }

    @Override
    public void removeLike(String videoId) throws MediaNotFoundException {
        Video video = videoRetrievalService.getRawVideo(videoId);
        if(video.getLikes() > 0){
            video.setLikes(video.getLikes()-1);
            this.videoRepository.save(video);
        }
    }

    @Override
    public void removeDislike(String videoId) throws MediaNotFoundException {
        Video video = videoRetrievalService.getRawVideo(videoId);
        if(video.getDislikes() > 0){
            video.setDislikes(video.getDislikes()-1);
            this.videoRepository.save(video);
        }

    }

    @Override
    public boolean dislikeVideo(String videoId) throws MediaNotFoundException {
        Video video = videoRetrievalService.getRawVideo(videoId);
        video.setDislikes(video.getDislikes()+1);
        this.videoRepository.save(video);
        log.info("Video disliked successfully!");
        return true;
    }

    @Override
    public void likeVideo(String videoId) throws MediaNotFoundException {
        Video video = videoRetrievalService.getRawVideo(videoId);
        video.setLikes(video.getLikes()+1);
        this.videoRepository.save(video);
        log.info("Video liked successfully!");
    }
}
