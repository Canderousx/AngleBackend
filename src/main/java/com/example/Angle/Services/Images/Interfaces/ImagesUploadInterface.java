package com.example.Angle.Services.Images.Interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface ImagesUploadInterface {

    boolean checkExtension(MultipartFile file);

    boolean checkExtension(File file);

    byte[] getBytesArray(MultipartFile file) throws IOException;

    byte[] getBytesArray(File file) throws IOException;




}
