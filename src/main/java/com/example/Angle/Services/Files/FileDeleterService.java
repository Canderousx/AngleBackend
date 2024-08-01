package com.example.Angle.Services.Files;

import com.example.Angle.Config.Exceptions.FileServiceException;
import com.example.Angle.Models.Video;
import com.example.Angle.Services.Files.Interfaces.FilesDeleterInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;


@Service
public class FileDeleterService implements FilesDeleterInterface {

    private final Logger logger = LogManager.getLogger(FileDeleterService.class);

    private boolean deleteRawFiles(String path){
        File file = new File(path);
        if(!file.isFile()){
            logger.error("Wrong raw file path. Aborting...");
            return false;
        }
        return file.delete();
    }

    private boolean deleteHlsFiles(String path) throws FileServiceException {
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
    @Override
    public void deleteVideoFiles(Video video) throws FileServiceException {
        logger.info("DELETER LAUNCHED");
        logger.info("VIDEO TO DELETE: "+video.getId());
        logger.info("Deleting raw files...");
        if(!deleteRawFiles(video.getRawPath())){
            logger.error("Unable to delete raw files...");
            throw new FileServiceException("RAW FILES NOT FOUND");
        };
        if(!deleteHlsFiles(video.getHlsPath())){
            logger.error("Unable to delete hls files...");
            throw new FileServiceException("Unable to delete HLS files...");
        }

    }
}
