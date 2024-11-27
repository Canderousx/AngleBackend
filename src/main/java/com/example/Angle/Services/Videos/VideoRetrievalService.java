package com.example.Angle.Services.Videos;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.Videos.Interfaces.VideoRetrievalInterface;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @Autowired
    public VideoRetrievalService(AccountRetrievalService accountRetrievalService,
                                 VideoRepository videoRepository,
                                 VideoThumbnailsService videoThumbnailsService) {
        this.accountRetrievalService = accountRetrievalService;
        this.videoRepository = videoRepository;
        this.videoThumbnailsService = videoThumbnailsService;
    }

    private void addRandomVideos(List<Video>currentList, String currentId){
        if(currentList.size() < 10){
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
    public Page<Video> getAllVideos(int page) {
        Pageable paginateSettings = PageRequest.of(page,12, Sort.by("datePublished").descending());
        Page<Video> videos = this.videoRepository.findAllByThumbnailIsNotNullAndNameIsNotNullAndIsBannedFalse(paginateSettings);
        if(!videos.getContent().isEmpty()){
            videoThumbnailsService.processThumbnails(videos.getContent());
        }
        return videos;
    }

    @Override
    public Page<Video> getUserVideos(String userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page,pageSize,Sort.by("datePublished").descending());
        Page<Video>userVideos = this.videoRepository.findByAuthorId(userId,pageable);
        if(userVideos.isEmpty()){
            return null;
        }
        videoThumbnailsService.processThumbnails(userVideos.getContent());
        return userVideos;
    }

    @Override
    public Video getVideo(String videoId) throws MediaNotFoundException, IOException, ClassNotFoundException {
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null){
            videoThumbnailsService.processThumbnail(video);
            return video;
        }
        throw new MediaNotFoundException("Video not found");
    }

    @Override //Returns a video without thumbnail, made for cases that doesn't require a thumbnail to be seen.
    public Video getRawVideo(String videoId) throws MediaNotFoundException {
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null){
            return video;
        }
        throw new MediaNotFoundException("Video not found");
    }

    @Override
    public List<Video> getMostPopular() {
        List<Video> mostPopular = videoRepository.findMostPopular();
        videoThumbnailsService.processThumbnails(mostPopular);
        return mostPopular;
    }

    @Override
    public Page<Video> getRandomBySubscribers(int page) throws BadRequestException {
        Account account = accountRetrievalService.getCurrentUser();
        if(account.getSubscribedIds().isEmpty()){
            return Page.empty();
        }
        Pageable pageable = PageRequest.of(page,12);
        String[] Strings = account.getSubscribedIds().toArray(new String[0]);
        Page<Video> videos = videoRepository.findFromSubscribers(Strings,pageable);
        videoThumbnailsService.processThumbnails(videos.getContent());
        return videos;
    }

    @Override
    public List<Video> getSimilar(String videoId) throws MediaNotFoundException {Video video = getRawVideo(videoId);
        Set<String> tagNames = new HashSet<>();
        video.getTags().forEach(tag -> tagNames.add(tag.getName()));
        List<Video> videos = videoRepository.findSimilar(tagNames,videoId);
        addRandomVideos(videos,videoId);
        if(!videos.isEmpty()){
            videoThumbnailsService.processThumbnails(videos);
            return videos;
        }
        return new ArrayList<>();
    }

    @Override
    public int checkRated(String videoId) throws BadRequestException {
        Account account = accountRetrievalService.getCurrentUser();
        if(account.getLikedVideos().contains(videoId)){
            return 1;
        }
        if(account.getDislikedVideos().contains(videoId)){
            return 2;
        }
        return 0;
    }

    @Override
    public int howManyUserVideos(String userId){
        return videoRepository.countUserVideos(userId);
    }


}
