package com.example.Angle.Services.Videos.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Video;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface VideoRetrievalInterface {

    Page<Video> getAllVideos(int page);

    Page<Video> getUserVideos(String userId, int page, int pageSize);

    Video getVideo(String videoId) throws MediaNotFoundException, IOException, ClassNotFoundException;

    Video getRawVideo(String videoId) throws MediaNotFoundException;

    List<Video>getMostPopular();

    Page<Video>getRandomBySubscribers(int page) throws BadRequestException;

    List<Video> getSimilar(String videoId) throws MediaNotFoundException;

    int howManyUserVideos(String userId);

    int checkRated(String videoId) throws BadRequestException;



}
