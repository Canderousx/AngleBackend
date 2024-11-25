package com.example.Angle.Services.Videos.Interfaces;

import com.example.Angle.Models.Video;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VideoSearchInterface {

    List<Video> getVideosByTag(String tag);

    Page<Video> findVideos(String query, int page);

    List<String>searchHelper(String query);
}
