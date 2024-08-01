package com.example.Angle.Services.Images;

import com.example.Angle.Config.SecServices.EnvironmentVariables;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Services.Images.Interfaces.ImagesSaveInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;


@Service
public class ImageSaveService implements ImagesSaveInterface {

    private final Logger logger = LogManager.getLogger(ImageSaveService.class);

    private final EnvironmentVariables environmentVariables;


    @Autowired
    public ImageSaveService(EnvironmentVariables environmentVariables){
        this.environmentVariables = environmentVariables;
    }
    @Override
    public void saveImage(String path, String base64Content) throws IOException {
        Thumbnail image = new Thumbnail(base64Content);
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(image);
        os.close();
        fos.close();
        logger.info("Image serialized successfully!");

    }

    @Override
    public String saveVideoThumbnail(String base64Content, String videoId) throws IOException {
        File folder = new File(environmentVariables.getThumbnailsPath()+"\\"+videoId);
        if(folder.mkdir()){
            logger.info("Created folder for {"+videoId+"} thumbnails");
        }
        String filename = folder.getPath()+"\\"+videoId+".tb";
        this.saveImage(filename,base64Content);
        return filename;
    }

    @Override
    public String saveUserAvatar(String base64Content, String userId) throws IOException {
        File folder = new File(environmentVariables.getAvatarsPath()+"\\"+userId);
        if(folder.mkdir()){
            logger.info("Created folder for {"+userId+"} avatars");
        }
        String filename = folder.getPath()+"\\"+userId+".at";
        this.saveImage(filename,base64Content);
        return filename;
    }
}
