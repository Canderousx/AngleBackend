package com.example.Angle.Config.Init;


import com.example.Angle.Config.Models.EnvironmentVariables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentVariablesInit {

    private final Logger logger = LogManager.getLogger(EnvironmentVariablesInit.class);

    @Bean
    EnvironmentVariables environmentVariables(){
        logger.info("Getting required System Environment Variables");
        EnvironmentVariables env = EnvironmentVariables.builder()
                .secretKey(System.getenv("JTOKEN_KEY"))
                .ffmpegPath(System.getenv("FFMPEG_PATH"))
                .hlsOutputPath(System.getenv("HLS_OUTPUT_PATH"))
                .ffmpegTempThumbnailsPath(System.getenv("FFMPEG_TEMP_THUMBNAILS_PATH"))
                .ffmpegTempFolder(System.getenv("FFMPEG_TEMP_FOLDER"))
                .avatarsPath(System.getenv("ANGLE_AVATARS_PATH"))
                .thumbnailsPath(System.getenv("ANGLE_THUMBNAILS_PATH"))
                .rawFilesPath(System.getenv("ANGLE_RAW_FILES_PATH"))
                .hlsFilesPath(System.getenv("ANGLE_HLS_FILES_PATH"))
                .frontUrl(System.getenv("ANGLE_FRONT_URL"))
                .build();
        if(!env.checkIfNotNull()){
            logger.error("ERROR: SOME OF YOUR SYSTEM VARIABLES DON'T EXIST. PLEASE CHECK IT OUT IMMEDIATELY");
            throw new RuntimeException();
        }
        logger.info("System variables loaded successfully");
        return env;
    }
}
