package com.example.Angle.Services.Videos;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.Videos.Interfaces.VideoRetrievalInterface;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class VideoRetrievalService implements VideoRetrievalInterface {

    private final AccountRetrievalService accountRetrievalService;

    private final VideoRepository videoRepository;

    private final VideoThumbnailsService videoThumbnailsService;

    private final Logger log = LogManager.getLogger(VideoRetrievalService.class);

    @Autowired
    public VideoRetrievalService(AccountRetrievalService accountRetrievalService,
                                 VideoRepository videoRepository,
                                 VideoThumbnailsService videoThumbnailsService) {
        this.accountRetrievalService = accountRetrievalService;
        this.videoRepository = videoRepository;
        this.videoThumbnailsService = videoThumbnailsService;
    }

    private void addRandomVideos(List<Video>currentList, String currentId){
        log.info("CURRENT VIDEO LIST SIZE: "+currentList.size());
        if(currentList.size() < 10){
            log.info("Less than 10... Adding some random movies!");
            List<String>alreadyIds = new ArrayList<>();
            if(currentList.isEmpty()){
                alreadyIds.add("");
            }else{
                currentList.forEach(video -> {
                    alreadyIds.add(video.getId());
                });
            }
            currentList.addAll(videoRepository.findRandom(alreadyIds,currentId,(10-currentList.size())));
        }
    }

    @Override
    public List<Video> getAllVideos(int page) {
        Pageable paginateSettings = PageRequest.of(page,12, Sort.by("datePublished").descending());
        List<Video> videos = this.videoRepository.findAllByThumbnailIsNotNullAndNameIsNotNullAndIsBannedFalse(paginateSettings).stream().toList();
        if(!videos.isEmpty()){
            videoThumbnailsService.processThumbnails(videos);
        }
        return videos;
    }

    @Override
    public List<Video> getUserVideos(String userId, Pageable pageable) {
        List<Video>userVideos = this.videoRepository.findByAuthorId(userId,pageable);
        if(userVideos.isEmpty()){
            log.info("User ["+userId+"] doesn't have any videos.");
            return null;
        }
        log.info("User ["+userId+"] videos found");
        videoThumbnailsService.processThumbnails(userVideos);
        return userVideos;
    }

    @Override
    public Video getVideo(String videoId) throws MediaNotFoundException, IOException, ClassNotFoundException {
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null){
            log.info("Requested Video FOUND: "+videoId);
            videoThumbnailsService.processThumbnail(video);
            return video;
        }
        log.info("Requested Video NOT FOUND: "+videoId);
        throw new MediaNotFoundException("Video not found");
    }

    @Override //Returns a video without thumbnail in base64, made for cases that doesn't require a thumbnail to be seen.
    public Video getRawVideo(String videoId) throws MediaNotFoundException {
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null){
            log.info("Requested Video FOUND: "+videoId);
            return video;
        }
        log.info("Requested Video NOT FOUND: "+videoId);
        throw new MediaNotFoundException("Video not found");
    }

    @Override
    public List<Video> getMostPopular() {
        List<Video> mostPopular = videoRepository.findMostPopular();
        videoThumbnailsService.processThumbnails(mostPopular);
        return mostPopular;
    }

    @Override
    public List<Video> getRandomBySubscribers(int page) throws BadRequestException {
        Account account = accountRetrievalService.getCurrentUser();
        if(account.getSubscribedIds().isEmpty()){
            return new ArrayList<>();
        }
        Pageable pageable = PageRequest.of(page,12);
        String[] Strings = account.getSubscribedIds().toArray(new String[0]);
        List<Video> videos = videoRepository.findFromSubscribers(Strings,pageable).stream().toList();
        videoThumbnailsService.processThumbnails(videos);
        return videos;
    }

    @Override
    public List<Video> getSimilar(String videoId) throws MediaNotFoundException {        Video video = getRawVideo(videoId);
        Set<String> tagNames = new HashSet<>();
        video.getTags().forEach(tag -> tagNames.add(tag.getName()));
        List<Video> videos = videoRepository.findSimilar(tagNames,videoId);
        addRandomVideos(videos,videoId);
        if(!videos.isEmpty()){
            videoThumbnailsService.processThumbnails(videos);
            return videos;
        }
        log.info("No similar videos present in a list!");
        return new ArrayList<>();
    }

    @Override
    public int howManyUserVideos(String userId){
        return videoRepository.countUserVideos(userId);
    }
}
