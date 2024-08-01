package com.example.Angle.Services.Images;


import com.example.Angle.Config.SecServices.EnvironmentVariables;
import com.example.Angle.Services.Images.Interfaces.ImagesUploadInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class ImageUploadService implements ImagesUploadInterface {

    private final Logger logger = LogManager.getLogger(ImageUploadService.class);

    private final String[] allowedExtensions = {"jpg","jpeg","webp"};
    @Override
    public boolean checkExtension(MultipartFile file) {
        if(file.getOriginalFilename() != null){
            logger.info("Checking file extension: "+file.getOriginalFilename());
            for(String extension : allowedExtensions){
                if(file.getOriginalFilename().contains(extension)){
                    logger.info("File extension supported!");
                    return true;
                }
            }
        }else{
            logger.error("ERROR: file original name is NULL!");
        }
        logger.error("Error: File extension not supported!");
        return false;
    }

    @Override
    public boolean checkExtension(File file) {
            logger.info("Checking file extension: "+file.getName());
            for(String extension : allowedExtensions){
                if(file.getName().contains(extension)){
                    logger.info("File extension supported!");
                    return true;
                }
            }
        logger.error("Error: File extension not supported!");
        return false;
    }

    @Override
    public byte[] getBytesArray(MultipartFile file) throws IOException {
        if(checkExtension(file)){
            return file.getBytes();
        }
        throw new InvalidFileNameException("","File extension not supported");
    }

    @Override
    public byte[] getBytesArray(File file) throws IOException {
        if(checkExtension(file)){
            return Files.readAllBytes(file.toPath());
        }
        throw new InvalidFileNameException("","File extension not supported");
    }


}
