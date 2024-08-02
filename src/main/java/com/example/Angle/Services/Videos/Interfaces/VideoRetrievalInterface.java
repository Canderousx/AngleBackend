package com.example.Angle.Services.Videos.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Video;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface VideoRetrievalInterface {

    List<Video> getAllVideos(int page);

    List<Video> getUserVideos(String userId, Pageable pageable);

    Video getVideo(String videoId) throws MediaNotFoundException, IOException, ClassNotFoundException;

    Video getRawVideo(String videoId) throws MediaNotFoundException;

    List<Video>getMostPopular();

    List<Video>getRandomBySubscribers(int page) throws BadRequestException;

    List<Video> getSimilar(String videoId) throws MediaNotFoundException;

    int howManyUserVideos(String userId);



}