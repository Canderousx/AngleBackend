package com.example.Angle.Services.FFMpeg;

import com.example.Angle.Config.Exceptions.FileStoreException;
import com.example.Angle.Config.SecServices.EnvironmentVariables;
import com.example.Angle.Services.FFMpeg.Interfaces.FFMpegConverterInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

@Service
public class FFMpegConverterService implements FFMpegConverterInterface {

    private final Logger logger = LogManager.getLogger(FFMpegConverterService.class);

    private final EnvironmentVariables environmentVariables;

    @Autowired
    public FFMpegConverterService(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    @Async
    @Override
    public CompletableFuture<Void> convertToHls(String filePath, String videoId) {
        File newFolder = new File(environmentVariables.getHlsOutputPath()+"\\"+videoId);
        if(newFolder.mkdir()){
            logger.info("New Folder ["+videoId+"] created!");
        };
        return CompletableFuture.runAsync(() -> {
            String playlistName = videoId+"_playlist.m3u8";
            try {
                String command = String.format(
                        "%s -i %s " +
                                "-map 0:v -map 0:a -b:v:0 5000k -c:v:0 libx264 -vf \"scale=-2:min(1080\\,ih)\" -b:a:0 192k " +
                                "-map 0:v -map 0:a -b:v:1 2800k -c:v:1 libx264 -vf \"scale=-2:min(720\\,ih)\" -b:a:1 192k " +
                                "-map 0:v -map 0:a -b:v:2 1400k -c:v:2 libx264 -vf \"scale=-2:min(480\\,ih)\" -b:a:2 128k " +
                                "-var_stream_map \"v:0,a:0 v:1,a:1 v:2,a:2\" " +
                                "-master_pl_name %s " +
                                "-f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%v_%%03d.ts\" %s/variant_%%v_%s.m3u8",
                        environmentVariables.getFfmpegPath(),filePath, playlistName, newFolder.getAbsolutePath(), newFolder.getAbsolutePath(), videoId);
                ProcessBuilder builder = new ProcessBuilder(command.split(" "));
                builder.redirectErrorStream(true);
                Process process = builder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info(line);
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    logger.info("Conversion to HLS for videoId " + videoId + " completed successfully.");
                } else {
                    logger.error("FFmpeg conversion process for videoId " + videoId + " exited with error code: " + exitCode);
                    throw new FileStoreException("An error occurred during the conversion process for videoId " + videoId);
                }
            } catch (Exception e) {
                logger.error("An error occurred during the conversion process for videoId " + videoId, e);
            }
        });
    }
}
