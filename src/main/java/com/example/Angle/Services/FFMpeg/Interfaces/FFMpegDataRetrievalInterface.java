package com.example.Angle.Services.FFMpeg.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Thumbnail;

import java.io.IOException;
import java.util.List;



public interface FFMpegDataRetrievalInterface {

    double getVideoDuration(String rawPath) throws IOException, InterruptedException;

    List<Thumbnail> getVideoThumbnails(String rawPath) throws MediaNotFoundException, IOException, ClassNotFoundException, InterruptedException;


    List<Thumbnail> generateVideoThumbnails(String rawPath) throws IOException, InterruptedException;
}
