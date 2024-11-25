package com.example.Angle.Services.Videos.Interfaces;

import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Video;

public interface VideoModerationInterface {

    void registerView(String videoId) throws MediaNotFoundException;

    void removeVideo(String id) throws MediaNotFoundException, FileServiceException;

    void banVideo(String videoId) throws MediaNotFoundException;

    void unbanVideo(String videoId) throws MediaNotFoundException;

    void removeLike(String videoId) throws MediaNotFoundException;

    void removeDislike(String videoId) throws MediaNotFoundException;

    boolean dislikeVideo(String videoId) throws MediaNotFoundException;

    void likeVideo(String videoId) throws MediaNotFoundException;

    void setMetadata(String id, Video metadata) throws MediaNotFoundException;


}
