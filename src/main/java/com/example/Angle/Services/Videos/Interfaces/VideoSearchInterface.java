package com.example.Angle.Services.Videos.Interfaces;

import com.example.Angle.Models.Video;

import java.util.List;

public interface VideoSearchInterface {

    List<Video> getVideosByTag(String tag);

    List<Video> findVideos(String query,int page);

    List<String>searchHelper(String query);
}
