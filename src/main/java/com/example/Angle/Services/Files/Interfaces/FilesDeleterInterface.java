package com.example.Angle.Services.Files.Interfaces;

import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Models.Video;

public interface FilesDeleterInterface {

    void deleteVideoFiles(Video video) throws FileServiceException;




}
