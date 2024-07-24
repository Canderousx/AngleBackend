package com.example.Angle.Services;


import com.example.Angle.Models.Thumbnail;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

@Service
public class ImageService {

    private final Logger logger = LogManager.getLogger(ImageService.class);

    private final String thumbnailPath = "E:\\IT\\Angle\\BACKEND\\Angle\\src\\main\\resources\\media\\thumbnails";

    private final String avatarPath = "E:\\IT\\Angle\\BACKEND\\Angle\\src\\main\\resources\\media\\avatars";

    private final String[] allowedExtensions = {"jpg","jpeg","webp"};

    public boolean checkExtension(MultipartFile file){
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

    public String processAvatar(MultipartFile file) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(150,150)
                .outputQuality(0.8)
                .toOutputStream(byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(imgBytes);

    }

    public String imageToBase64(File file) throws IOException {
        byte[] imgBytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(imgBytes);
    }
    public String imageToBase64(MultipartFile multipartFile) throws IOException {
        byte[] imgBytes = multipartFile.getBytes();
        return Base64.getEncoder().encodeToString(imgBytes);
    }

    private void saveImage(String path,String content) throws IOException {
        Thumbnail image = new Thumbnail(content);
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(image);
        os.close();
        fos.close();
        logger.info("Image serialized successfully!");
    }
    public Thumbnail readImage(String path) throws IOException, ClassNotFoundException {
        if(path == null){
            return new Thumbnail("");
        }
        FileInputStream fis = new FileInputStream(path);
        ObjectInputStream is = new ObjectInputStream(fis);
        Thumbnail image = (Thumbnail) is.readObject();
        is.close();
        fis.close();
        return image;
    }


    public String saveVideoThumbnail(String content,String videoId) throws IOException {
        File folder = new File(thumbnailPath+"\\"+videoId);
        if(folder.mkdir()){
            logger.info("Created folder for {"+videoId+"} thumbnails");
        }
        String filename = folder.getPath()+"\\"+videoId+".tb";
        this.saveImage(filename,content);
        return filename;
    }

    public String saveUserAvatar(String content, String userId) throws IOException {
        File folder = new File(avatarPath+"\\"+userId);
        if(folder.mkdir()){
            logger.info("Created folder for {"+userId+"} avatars");
        }
        String filename = folder.getPath()+"\\"+userId+".at";
        this.saveImage(filename,content);
        return filename;

    }
}