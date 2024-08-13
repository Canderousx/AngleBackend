package com.example.Angle.Services.FFMpeg;

import com.example.Angle.Config.Exceptions.FileStoreException;
import com.example.Angle.Config.Models.EnvironmentVariables;
import com.example.Angle.Services.FFMpeg.Interfaces.FFMpegConverterInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
        File newFolder = new File(environmentVariables.getHlsOutputPath() + "/" + videoId);
        if (newFolder.mkdir()) {
            logger.info("New Folder [" + videoId + "] created!");
        }

        return CompletableFuture.runAsync(() -> {
            String playlistName = videoId + "_playlist.m3u8";
            String segmentFileName = newFolder.getAbsolutePath() + "/segment_%v_%03d.ts";
            String variantPlaylistName = newFolder.getAbsolutePath() + "/variant_%v_" + videoId + ".m3u8";
            try {
                String[] command = {
                        environmentVariables.getFfmpegPath(),
                        "-i", filePath,
                        "-map", "0:v", "-map", "0:a", "-b:v", "5000k", "-c:v", "libx264", "-vf", "scale=-2:1080", "-b:a", "192k",
                        "-map", "0:v", "-map", "0:a", "-b:v", "2800k", "-c:v", "libx264", "-vf", "scale=-2:720", "-b:a", "192k",
                        "-map", "0:v", "-map", "0:a", "-b:v", "1400k", "-c:v", "libx264", "-vf", "scale=-2:480", "-b:a", "128k",
                        "-var_stream_map", "v:0,a:0 v:1,a:1 v:2,a:2",
                        "-master_pl_name", playlistName,
                        "-f", "hls", "-hls_time", "10", "-hls_list_size", "0",
                        "-hls_segment_filename", segmentFileName,
                        variantPlaylistName
                };
                //to lower cpu usage:
//                String[] command = {
//                        environmentVariables.getFfmpegPath(),
//                        "-i", filePath,
//                        "-map", "0:v", "-map", "0:a", "-b:v", "1500k", "-c:v", "libx264", "-vf", "scale=-2:720", "-b:a", "128k",
//                        "-map", "0:v", "-map", "0:a", "-b:v", "800k", "-c:v", "libx264", "-vf", "scale=-2:480", "-b:a", "128k",
//                        "-var_stream_map", "v:0,a:0 v:1,a:1",
//                        "-master_pl_name", playlistName,
//                        "-f", "hls", "-hls_time", "10", "-hls_list_size", "0",
//                        "-hls_segment_filename", segmentFileName,
//                        variantPlaylistName
//                };
                //for weaker cpus:
//                String[] command = {
//                        environmentVariables.getFfmpegPath(),
//                        "-i", filePath,
//                        "-map", "0:v", "-map", "0:a",
//                        "-b:v", "800k", "-c:v", "libx264", "-profile:v", "baseline", "-level", "3.0", "-vf", "scale=-2:480", "-r", "24", "-g", "48", "-b:a", "96k",
//                        "-f", "hls", "-hls_time", "10", "-hls_list_size", "0",
//                        "-hls_segment_filename", segmentFileName,
//                        variantPlaylistName
//                };

                ProcessBuilder builder = new ProcessBuilder(command);
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
                    throw new RuntimeException("An error occurred during the conversion process for videoId " + videoId);
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Error during the HLS conversion process", e);
                throw new RuntimeException(e);
            }
        });
    }

}
