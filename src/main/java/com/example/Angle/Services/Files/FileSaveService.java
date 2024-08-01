package com.example.Angle.Services.Files;


import com.example.Angle.Config.Exceptions.FileStoreException;
import com.example.Angle.Config.SecServices.EnvironmentVariables;
import com.example.Angle.Services.Files.Interfaces.FilesSaveInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileSaveService implements FilesSaveInterface {

    private final Logger logger = LogManager.getLogger(FileDeleterService.class);

    private final EnvironmentVariables environmentVariables;

    @Autowired
    public FileSaveService(EnvironmentVariables environmentVariables){
        this.environmentVariables = environmentVariables;
    }
    @Override
    public String saveRawFile(MultipartFile file) throws FileStoreException {
        if(file.isEmpty()){
            logger.error("File sent to save is empty!");
            throw new FileStoreException("File sent to save is empty!");
        };
        String savedFileName = UUID.randomUUID()+"_"+UUID.randomUUID()+".mp4";
        Path destinationFile = Paths
                .get(environmentVariables.getRawFilesPath())
                .resolve(Paths.get(savedFileName))
                .normalize()
                .toAbsolutePath();
        if(!destinationFile.getParent().equals(Paths.get(environmentVariables.getRawFilesPath()).toAbsolutePath())){
            logger.error("File cannot be saved outside rawFiles directory!");
            throw new FileStoreException("File cannot be saved outside rawFiles directory!");
        }
        try {
            file.transferTo(destinationFile);
            logger.info("File has been saved!");
            return destinationFile.toString();
        } catch (IOException e) {
            logger.error("Couldn't save the file...");
            throw new FileStoreException("File cannot be saved outside rawFiles directory!");
        }
    }
}
