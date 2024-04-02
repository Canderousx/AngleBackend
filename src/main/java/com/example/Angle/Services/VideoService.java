package com.example.Angle.Services;


import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.VideoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoService {

    private final Logger log = LogManager.getLogger(VideoService.class);

    @Autowired
    private VideoRepository videoRepository;

    public void addVideo(Video video){
        if(video!=null){
            this.videoRepository.save(video);
            log.info("New video saved: "+video.getName());
        }else{
            log.error("Unable to save new video. It's NULL");
        }

    }

    public void removeVideo(UUID videoId){
        Optional<Video> toRemove = this.videoRepository.findById(videoId);
        if(toRemove.isPresent()){
            this.videoRepository.delete(toRemove.get());
            log.info("Video ["+videoId+"] has been removed");
        }else{
            log.error("Requested video ["+videoId+"] NOT FOUND!");
        }
    }

    public void banVideo(UUID videoId){
        Video toBan = this.videoRepository.findById(videoId).orElse(null);
        if(toBan !=null){
            toBan.setBanned(true);
            this.videoRepository.save(toBan);
            log.info("Video has been banned: "+toBan.getId());
        }else{
            log.error("Requested Video NOT FOUND: "+videoId);
        }
    }

    public void unbanVideo(UUID videoId){
        Video toUnban = this.videoRepository.findById(videoId).orElse(null);
        if(toUnban !=null){
            toUnban.setBanned(false);
            this.videoRepository.save(toUnban);
            log.info("Video has been unbanned: "+toUnban.getId());
        }else{
            log.error("Requested Video NOT FOUND: "+videoId);
        }
    }

    public List<Video> getUserVideos(UUID userId){
        Optional<Video>userVideos = this.videoRepository.findByAuthorId(userId);
        if(userVideos.isPresent()){
            log.info("User ["+userId+"] videos found");
            return new ArrayList<>(userVideos.stream().toList());
        }
        log.info("User ["+userId+"] doesn't have any videos.");
        return null;
    }

    public Video getVideo(UUID videoId){
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null){
            log.info("Requested Video FOUND: "+videoId);
            return video;
        }
        log.info("Requested Video NOT FOUND: "+videoId);
        return null;
    }

    public List<Video> getVideosByTag(String tag){
        Optional<Video>tagsVideos = this.videoRepository.findByTag(tag);
        if(tagsVideos.isPresent()){
            log.info("Requested Tags Videos FOUND");
            return new ArrayList<>(tagsVideos.stream().toList());
        }
        log.info("Requested tag ["+tag+"] have no usage!");
        return null;
    }

    public boolean likeVideo(UUID videoId){
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null){
            video.setLikes(video.getLikes()+1);
            this.videoRepository.save(video);
            log.info("Video liked successfully!");
            return true;
        }else{
            log.error("Requested video not found!");
            return false;
        }
    }
    public boolean dislikeVideo(UUID videoId){
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null){
            video.setDislikes(video.getDislikes()+1);
            this.videoRepository.save(video);
            log.info("Video disliked successfully!");
            return true;
        }else{
            log.error("Requested video not found!");
            return false;
        }
    }
}
