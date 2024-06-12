package com.example.Angle.Services;


import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Models.Comment;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.TagRepository;
import com.example.Angle.Repositories.VideoRepository;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class VideoService {

    private final Logger log = LogManager.getLogger(VideoService.class);

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    ImageService imageService;

    @Autowired
    FileService fileService;

    @Autowired
    CommentService commentService;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AccountRepository accountRepository;


    public void registerView(UUID videoId){
        Optional<Video> toView = this.videoRepository.findById(videoId);
        if(toView.isPresent()){
            Video video = toView.get();
            video.setViews(video.getViews()+1);
            videoRepository.save(video);
        }else{
            log.error("Requested video ["+videoId+"] NOT FOUND!");
        }
    }


    public boolean removeVideo(Video video){
        if(!fileService.deleteVideoFiles(video)){
           log.error("Video deletion failed.");
        }
        log.info("Files removed successfully! Removing database entries");
        commentService.removeVideoComments(video.getId());


        removeLikeInteractions(video.getId());
        removeDislikeInteractions(video.getId());

        videoRepository.deleteTagAssociations(video.getId());
        videoRepository.delete(video);
        log.info("DB cleaned.");
        return true;

    }

    private void removeDislikeInteractions(UUID videoId){
        List<Account> likedAccounts = accountRepository.findUsersWhoDislikeVideo(videoId);
        if(likedAccounts.isEmpty()){
            log.info("No one disliked requested video");
            return;
        }
        likedAccounts.forEach(account ->{
            account.getDislikedVideos().remove(videoId);
            accountRepository.save(account);
        });
        log.info("Dislike Interactions removed successfully");
    }


    private void removeLikeInteractions(UUID videoId){
        log.info("Removing accounts interactions");
        List<Account> likedAccounts = accountRepository.findUsersWhoLikeVideo(videoId);
        if(likedAccounts.isEmpty()){
            log.info("No one liked requested video");
            return;
        }
        likedAccounts.forEach(account ->{
            account.getLikedVideos().remove(videoId);
            accountRepository.save(account);
        });
        log.info("Like Interactions removed successfully");
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

    private void processThumbnails(List<Video> toProcess){
        toProcess.forEach(video ->{
            try {
                video.setAuthorAvatar(
                        imageService.readImage(
                                accountRepository.findById(video.getAuthorId()).get().getAvatar()
                        ).getContent()
                );
                video.setThumbnail(
                        imageService.readImage(
                                video.getThumbnail()
                        ).getContent()
                );
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
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

    public List<Video> getUserVideos(UUID userId,Pageable pageable) throws BadRequestException {
        Account account = accountRepository.findById(userId).orElse(null);
        if(account == null){
            log.error("USER NOT FOUND!");
            throw new BadRequestException();
        }
        List<Video>userVideos = this.videoRepository.findByAuthorId(userId,pageable);
        if(userVideos.isEmpty()){
            log.info("User ["+userId+"] doesn't have any videos.");
            return null;
        }
        log.info("User ["+userId+"] videos found");
        userVideos.forEach(v ->{
            try {
                v.setThumbnail(imageService.readImage(v.getThumbnail()).getContent());
                v.setAuthorAvatar(imageService.readImage(account.getAvatar()).getContent());
            } catch (IOException | ClassNotFoundException e) {
                v.setThumbnail(null);
                log.error("Couldn't load video thumbnail!");
            }
        });
        return userVideos;
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

    public void removeLike(UUID videoId){
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null && video.getLikes() > 0){
            log.info("Previous likes: "+video.getLikes());
            log.info("After removal: "+(video.getLikes()-1));
            video.setLikes(video.getLikes()-1);
            this.videoRepository.save(video);
        }else{
            log.error("Requested video not found!");
        }
    }
    public void removeDislike(UUID videoId){
        Video video = this.videoRepository.findById(videoId).orElse(null);
        if(video!=null && video.getDislikes() > 0){
            video.setDislikes(video.getDislikes()-1);
            this.videoRepository.save(video);
        }else{
            log.error("Requested video not found!");
        }
    }

    public List<Video>findVideos(String query,int page){
        List<Video> found = new ArrayList<>();
        Pageable pageable = Pageable.ofSize(12);
        found = videoRepository.findByNameContainingOrTagsNameContaining(query,query,query,pageable).stream().toList();



        processThumbnails(found);
        return found;
    }

    public List<String>searchHelper(String query){
        List<String>helpers = new ArrayList<>();
        helpers = tagRepository.findNameContaining(query);
        helpers.addAll(videoRepository.findNameContaining(query));
        return helpers;
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

    private List<Video> addRandomVideos(List<Video>currentList, UUID currentId){
        List<UUID>alreadyIds = new ArrayList<>();
        currentList.forEach(video -> {
            alreadyIds.add(video.getId());
        });
        currentList.addAll(videoRepository.findRandom(alreadyIds,currentId,(10-currentList.size())));
        return currentList;

    }

    public List<Video>getRecommended(){

        return new ArrayList<>();
    }

    public List<Video> getSimilar(UUID videoId){
        Video video = videoRepository.findById(videoId).orElse(null);
        if(video!=null){
            Set<String>tagNames = new HashSet<>();
            video.getTags().forEach(tag -> tagNames.add(tag.getName()));
            List<Video> videos = videoRepository.findSimilar(tagNames,videoId);
            if(videos.isEmpty()){
                log.info("Similar videos not found. Getting random");
                videos = videoRepository.findRandom(new ArrayList<>(Collections.singleton(video.getId())),videoId,10);
            }

            if(!videos.isEmpty()){
                if(videos.size() < 10){
                    addRandomVideos(videos,videoId);
                }

                videos.forEach(v -> {
                    try {
                        log.info("THUMBNAIL OF VIDEO ID: "+v.getId());
                        v.setThumbnail(imageService.readImage(v.getThumbnail()).getContent());
                        v.setAuthorAvatar(
                                imageService.readImage(
                                        accountRepository.findById(v.getAuthorId())
                                                .get()
                                                .getAvatar())
                                        .getContent()
                        );
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
                return videos;
            }
            log.info("No videos present in a list!");

        }
        return new ArrayList<>();
    }
}
