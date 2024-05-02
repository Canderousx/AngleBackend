package com.example.Angle.Services;

import com.example.Angle.Config.Exceptions.FileStoreException;
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
