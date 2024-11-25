package com.example.Angle.Services.Videos.Interfaces;

import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.FileStoreException;
import org.apache.coyote.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

public interface VideoUploadInterface {

    void uploadVideo(MultipartFile file) throws BadRequestException, FileStoreException, FileServiceException;
}
