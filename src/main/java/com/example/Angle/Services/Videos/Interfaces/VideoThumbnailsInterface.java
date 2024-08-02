package com.example.Angle.Services.Videos.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Video;

import java.io.IOException;
import java.util.List;

public interface VideoThumbnailsInterface {

    void processThumbnail(Video video) throws IOException, ClassNotFoundException, MediaNotFoundException;

    void processThumbnails(List<Video> toProcess);
}
