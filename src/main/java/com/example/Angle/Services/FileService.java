package com.example.Angle.Services;

import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Config.Exceptions.FileStoreException;
import com.example.Angle.Models.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;


@Service
public class FileService {

    private final Path rawFiles = Paths.get("E:\\IT\\Angle\\BACKEND\\Angle\\src\\main\\resources\\media\\raw");

    private final String hlsFiles = "E:\\IT\\Angle\\BACKEND\\Angle\\src\\main\\resources\\media\\hls";
    private final Logger logger = LogManager.getLogger(FileService.class);

    public boolean deleteRawFiles(String path){
        File file = new File(path);
        if(!file.isFile()){
            logger.error("Wrong raw file path. Aborting...");
            return false;
        }
        return file.delete();
    }

    public boolean deleteHlsFiles(String path) throws FileServiceException {
        File file = new File(path);
        File directory = file.getParentFile();
        if(!directory.isDirectory()){
            logger.error("Couldn't process provided path! Aborting operation");
            return false;
        }
        File[] files = directory.listFiles();
        if(files != null){
            for(File toRemove: files){
                logger.info("Removing: "+toRemove.getName());
                if(toRemove.delete()){
                    logger.info("File removed");
                }else{
                    logger.error("Couldn't remove file: "+toRemove.getName());
                }
            }
            return directory.delete();
        }
        logger.error("Files in dir: "+directory.getAbsolutePath()+" are null!");
        return false;
    }

    public void deleteVideoFiles(Video video) throws FileServiceException {
        logger.info("DELETER LAUNCHED");
        logger.info("VIDEO TO DELETE: "+video.getId());
        logger.info("Deleting raw files");
        if(!deleteRawFiles(video.getRawPath())){
            logger.error("Unable to delete raw files...");
            throw new FileServiceException("RAW FILES NOT FOUND");
        };
        if(!deleteHlsFiles(video.getHlsPath())){
            logger.error("Unable to delete hls files...");
            throw new FileServiceException("Unable to delete HLS files...");
        }
    }

    public String storeFile(MultipartFile file) throws FileStoreException {
        logger.info("storeFile method launched");
        if(file.isEmpty()){
            logger.error("File sent to save is empty!");
            throw new FileStoreException();
        }
        String originalName = file.getOriginalFilename();
        String fileName = UUID.randomUUID()+"_"+UUID.randomUUID()+".mp4";

        Path destinationFile = this.rawFiles.resolve(Paths.get(fileName)).normalize().toAbsolutePath();
        if(!destinationFile.getParent().equals(this.rawFiles.toAbsolutePath())){
            logger.error("File cannot be saved outside rawFiles directory!");
            throw new FileStoreException();
        }
        try {
            file.transferTo(destinationFile);
            logger.info("File has been saved!");
            return destinationFile.toString();
        } catch (IOException e) {
            logger.error("Couldn't save the file...");
            throw new FileStoreException();
        }
    }




}
