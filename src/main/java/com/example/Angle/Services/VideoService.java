package com.example.Angle.Services;


import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.TagRepository;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.Comments.CommentManagementServiceImpl;
import com.example.Angle.Services.Files.FileDeleterService;
import com.example.Angle.Services.Images.ImageRetrievalService;
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
public class VideoService {

    private final Logger log = LogManager.getLogger(VideoService.class);

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private ImageRetrievalService imageRetrievalService;

    @Autowired
    private FileDeleterService fileDeleterService;

    @Autowired
    private CommentManagementServiceImpl commentManagementServiceImpl;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AccountService accountService;


    public void registerView(String videoId) throws MediaNotFoundException, IOException, ClassNotFoundException {
        Video video = getRawVideo(videoId);
        video.setViews(video.getViews()+1);
        videoRepository.save(video);
    }

    public int howManyUserVideos(String userId){
        return videoRepository.countUserVideos(userId);
    }


    public void removeVideo(String id) throws MediaNotFoundException, FileServiceException{
        Video video = getRawVideo(id);
        fileDeleterService.deleteVideoFiles(video);
        log.info("Files removed successfully! Removing database entries");
        commentManagementServiceImpl.removeVideoComments(video.getId());
        accountService.removeLikeInteractions(video.getId());
        accountService.removeDislikeInteractions(video.getId());
        videoRepository.deleteTagAssociations(video.getId());
        videoRepository.delete(video);
        log.info("DB cleaned.");
    }



    public void banVideo(String videoId) throws MediaNotFoundException {
        Video toBan = getRawVideo(videoId);
        toBan.setBanned(true);
        this.videoRepository.save(toBan);
        log.info("Video has been banned: "+toBan.getId());
    }

    private void processThumbnails(List<Video> toProcess){
        toProcess.forEach(video ->{
            try {
                processThumbnail(video);
            } catch (IOException | ClassNotFoundException | MediaNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void processThumbnail(Video video) throws IOException, ClassNotFoundException, MediaNotFoundException {
        video.setAuthorAvatar(
                accountService.generateAccountResponse(video.getAuthorId()).getAvatar()
        );
        video.setThumbnail(
                imageRetrievalService.getImage(
                        video.getThumbnail()
                ).getContent()
        );
    }

    public void unbanVideo(String videoId) throws MediaNotFoundException {
        Video toUnban = getRawVideo(videoId);
        toUnban.setBanned(false);
        this.videoRepository.save(toUnban);
        log.info("Video has been unbanned: "+toUnban.getId());
    }

    public List<Video> getAllVideos(int page){
        Pageable paginateSettings = PageRequest.of(page,12, Sort.by("datePublished").descending());
        List<Video> videos = this.videoRepository.findAllByThumbnailIsNotNullAndNameIsNotNullAndIsBannedFalse(paginateSettings).stream().toList();
        if(!videos.isEmpty()){
            processThumbnails(videos);
        }
        return videos;
    }

    public List<Video> getUserVideos(String userId,Pageable pageable){
        List<Video>userVideos = this.videoRepository.findByAuthorId(userId,pageable);
        if(userVideos.isEmpty()){
            log.info("User ["+userId+"] doesn't have any videos.");
            return null;
        }
        log.info("User ["+userId+"] videos found");
        processThumbnails(userVideos);
        return userVideos;
    }

    public Video getVideo(String videoId) throws MediaNotFoundException, IOException, ClassNotFoundException {
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null){
            log.info("Requested Video FOUND: "+videoId);
            processThumbnail(video);
            return video;
        }
        log.info("Requested Video NOT FOUND: "+videoId);
        throw new MediaNotFoundException("Video not found");
    }
    public Video getRawVideo(String videoId) throws MediaNotFoundException {
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null){
            log.info("Requested Video FOUND: "+videoId);
            return video;
        }
        log.info("Requested Video NOT FOUND: "+videoId);
        throw new MediaNotFoundException("Video not found");
    }



    public List<Video> getVideosByTag(String tag){
        Optional<Video>tagsVideos = this.videoRepository.findByTag(tag);
        if(tagsVideos.isPresent()){
            log.info("Requested Tags Videos FOUND");
            List<Video>videos = new ArrayList<>(tagsVideos.stream().toList());
            processThumbnails(videos);
            return videos;
        }
        log.info("Requested tag ["+tag+"] has no usage!");
        return null;
    }

    public void likeVideo(String videoId) throws MediaNotFoundException {
        Video video = getRawVideo(videoId);
        video.setLikes(video.getLikes()+1);
        this.videoRepository.save(video);
        log.info("Video liked successfully!");
    }

    public void removeLike(String videoId) throws MediaNotFoundException {
        Video video = getRawVideo(videoId);
        if(video.getLikes() > 0){
            video.setLikes(video.getLikes()-1);
            this.videoRepository.save(video);
        }
    }
    public void removeDislike(String videoId) throws MediaNotFoundException {
        Video video = getRawVideo(videoId);
        if(video.getDislikes() > 0){
            video.setDislikes(video.getDislikes()-1);
            this.videoRepository.save(video);
        }
    }

    public List<Video>findVideos(String query,int page){
        List<Video> found;
        Pageable pageable = PageRequest.of(page,12);
        found = videoRepository.findByNameContainingOrTagsNameContaining(query,query,query,pageable).stream().toList();
        if(!found.isEmpty()){
            processThumbnails(found);
        }
        return found;
    }

    public List<Video>findMostPopular(){
        List<Video> mostPopular = videoRepository.findMostPopular();
        processThumbnails(mostPopular);
        return mostPopular;
    }

    public List<String>searchHelper(String query){
        List<String>helpers;
        helpers = tagRepository.findNameContaining(query);
        helpers.addAll(videoRepository.findNameContaining(query));
        return helpers;
    }

    public List<Video>getRandomBySubscribers(int page) throws BadRequestException {
        Account account = accountService.getCurrentUser();
        if(account.getSubscribedIds().isEmpty()){
            return new ArrayList<>();
        }
        Pageable pageable = PageRequest.of(page,12);
        String[] Strings = account.getSubscribedIds().toArray(new String[0]);
        List<Video> videos = videoRepository.findFromSubscribers(Strings,pageable).stream().toList();
        processThumbnails(videos);
        return videos;
    }

    public boolean dislikeVideo(String videoId) throws MediaNotFoundException {
        Video video = getRawVideo(videoId);
        video.setDislikes(video.getDislikes()+1);
        this.videoRepository.save(video);
        log.info("Video disliked successfully!");
        return true;
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
    public List<Video> getSimilar(String videoId) throws MediaNotFoundException {
        Video video = getRawVideo(videoId);
        Set<String>tagNames = new HashSet<>();
        video.getTags().forEach(tag -> tagNames.add(tag.getName()));
        List<Video> videos = videoRepository.findSimilar(tagNames,videoId);
        addRandomVideos(videos,videoId);
        if(!videos.isEmpty()){
            processThumbnails(videos);
            return videos;
        }
        log.info("No videos present in a list!");
        return new ArrayList<>();
    }
}
