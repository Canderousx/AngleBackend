package com.example.Angle.Services.Images.Interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface ImagesConverterInterface {

    String convertToBase64(File file) throws IOException;

    String convertToBase64(MultipartFile file) throws IOException;


}
