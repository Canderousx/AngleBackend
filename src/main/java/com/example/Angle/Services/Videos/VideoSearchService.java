package com.example.Angle.Services.Videos;

import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.TagRepository;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.Videos.Interfaces.VideoSearchInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class VideoSearchService implements VideoSearchInterface {

    private final VideoRepository videoRepository;

    private final VideoThumbnailsService videoThumbnailsService;

    private final TagRepository tagRepository;

    private final Logger log = LogManager.getLogger(VideoSearchService.class);

    @Autowired
    public VideoSearchService(VideoRepository videoRepository, VideoThumbnailsService videoThumbnailsService,
                              TagRepository tagRepository) {
        this.videoRepository = videoRepository;
        this.videoThumbnailsService = videoThumbnailsService;
        this.tagRepository = tagRepository;
    }

    @Override
    public List<Video> getVideosByTag(String tag) {
        Optional<Video> tagsVideos = this.videoRepository.findByTag(tag);
        if(tagsVideos.isPresent()){
            log.info("Requested Tags Videos FOUND");
            List<Video>videos = new ArrayList<>(tagsVideos.stream().toList());
            videoThumbnailsService.processThumbnails(videos);
            return videos;
        }
        log.info("Requested tag ["+tag+"] has no usage!");
        return null;
    }

    @Override
    public List<Video> findVideos(String query, int page) {
        List<Video> found;
        Pageable pageable = PageRequest.of(page,12);
        found = videoRepository.findByNameContainingOrTagsNameContaining(query,query,query,pageable).stream().toList();
        if(!found.isEmpty()){
            videoThumbnailsService.processThumbnails(found);
        }
        return found;
    }

    @Override
    public List<String> searchHelper(String query) {
        List<String>helpers;
        helpers = tagRepository.findNameContaining(query);
        helpers.addAll(videoRepository.findNameContaining(query));
        return helpers;
    }
}
