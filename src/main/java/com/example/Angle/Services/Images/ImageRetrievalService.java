package com.example.Angle.Services.Images;


import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Services.Images.Interfaces.ImagesRetrievalInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

@Service
public class ImageRetrievalService implements ImagesRetrievalInterface {
    private final Logger logger = LogManager.getLogger(ImageSaveService.class);
    @Override
    public Thumbnail getImage(String path) throws IOException, ClassNotFoundException {
        if(path == null){
            logger.error("Path is null!");
            return new Thumbnail("");
        }
        FileInputStream fis = new FileInputStream(path);
        ObjectInputStream is = new ObjectInputStream(fis);
        Thumbnail image = (Thumbnail) is.readObject();
        is.close();
        fis.close();
        return image;
    }
}
