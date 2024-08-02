package com.example.Angle.Services.Videos;

import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.Comments.CommentManagementService;
import com.example.Angle.Services.Files.FileDeleterService;
import com.example.Angle.Services.Videos.Interfaces.VideoModerationInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class VideoModerationService implements VideoModerationInterface {

    private final Logger log = LogManager.getLogger(VideoModerationService.class);

    private final VideoRetrievalService videoRetrievalService;

    private final VideoRepository videoRepository;

    private final FileDeleterService fileDeleterService;

    private final CommentManagementService commentManagementService;

    private final AccountService accountService;

    @Autowired
    public VideoModerationService(VideoRetrievalService videoRetrievalService,
                                  VideoRepository videoRepository,
                                  FileDeleterService fileDeleterService,
                                  CommentManagementService commentManagementService,
                                  AccountService accountService) {
        this.videoRetrievalService = videoRetrievalService;
        this.videoRepository = videoRepository;
        this.fileDeleterService = fileDeleterService;
        this.commentManagementService = commentManagementService;
        this.accountService = accountService;
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
        accountService.removeLikeInteractions(video.getId());
        accountService.removeDislikeInteractions(video.getId());
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
