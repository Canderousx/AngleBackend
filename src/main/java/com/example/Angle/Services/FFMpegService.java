package com.example.Angle.Services;


import com.example.Angle.Config.Exceptions.FileStoreException;
import com.example.Angle.Models.Thumbnail;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class FFMpegService {

    @Getter
    private final String outputPath = "E:\\IT\\Angle\\BACKEND\\Angle\\src\\main\\resources\\media\\hls";

    @Getter
    private final String thumbNailsPath = "E:\\IT\\Angle\\BACKEND\\Angle\\src\\main\\resources\\media\\temp\\thumbnail%03d.png";

    private final String tempFolder = "E:\\IT\\Angle\\BACKEND\\Angle\\src\\main\\resources\\media\\temp\\";
    private final Logger logger = LogManager.getLogger(FFMpegService.class);

    @Autowired
    ImageService imageService;

    private static String inputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line);
            }
        }
        return textBuilder.toString();
    }

    @Async
    public CompletableFuture<Void> convertToHls(String filePath, String videoId) throws FileStoreException {
        File newFolder = new File(outputPath+"\\"+videoId);
        if(newFolder.mkdir()){
            logger.info("New Folder ["+videoId+"] created!");
        };
        return CompletableFuture.runAsync(() -> {
            String playlistName = videoId+"_playlist.m3u8";
            try {
                String command = String.format(
                        "C:\\ffmpeg\\ffmpeg\\bin\\ffmpeg.exe -i %s " +
                                "-map 0:v -map 0:a -b:v:0 5000k -c:v:0 libx264 -vf \"scale=-2:min(1080\\,ih)\" -b:a:0 192k " +
                                "-map 0:v -map 0:a -b:v:1 2800k -c:v:1 libx264 -vf \"scale=-2:min(720\\,ih)\" -b:a:1 192k " +
                                "-map 0:v -map 0:a -b:v:2 1400k -c:v:2 libx264 -vf \"scale=-2:min(480\\,ih)\" -b:a:2 128k " +
                                "-var_stream_map \"v:0,a:0 v:1,a:1 v:2,a:2\" " +
                                "-master_pl_name %s " +
                                "-f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%v_%%03d.ts\" %s/variant_%%v_%s.m3u8",
                        filePath, playlistName, newFolder.getAbsolutePath(), newFolder.getAbsolutePath(), videoId);
                ProcessBuilder builder = new ProcessBuilder(command.split(" "));
                builder.redirectErrorStream(true); // Redirect error stream to output stream
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
                    throw new FileStoreException();
                }
            } catch (Exception e) {
                logger.error("An error occurred during the conversion process for videoId " + videoId, e);
            }
        });

    }



    public double getVideoDuration(String rawPath) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries",
                "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", rawPath);
        Process process = builder.start();
        String output = inputStreamToString(process.getInputStream());
        logger.info("Video duration: "+output);
        process.waitFor();
        return Double.parseDouble(output.trim());
    }


   public List<Thumbnail> getVideoThumbnails(String rawPath) throws IOException, InterruptedException {
        double videoLength = getVideoDuration(rawPath);
        File rawFile = new File(rawPath);
       Path path = rawFile.toPath();

       String processedPath = path.toString();
       System.out.println("PROCESSED PATH: "+processedPath);
       int framesNumber = (int) Math.floor(videoLength / 10);
       if(framesNumber > 5){
           framesNumber = 5;
       }

       List<String> command = Arrays.asList(
               "C:\\ffmpeg\\ffmpeg\\bin\\ffmpeg.exe", "-i", rawPath,
               "-vf", "fps=1/2,scale=320:-1",
               "-vframes", "5",
               "-compression_level", "6", "-preset", "photo",
//               "-vframes", "5", "-f", "image2",
               "-f", "image2",
               thumbNailsPath
       );


       ProcessBuilder builder = new ProcessBuilder(command);
       builder.redirectErrorStream(true);
       Process process = builder.start();
       try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
           String line;
           while ((line = reader.readLine()) != null) {
               logger.info(line);
           }
       }
       process.waitFor();
       if (process.exitValue() != 0) {
           logger.error("FFmpeg process failed with exit code " + process.exitValue());
       }
       List<Thumbnail> thumbnails = new ArrayList<>();

       for(int i = 0; i < framesNumber; i++){
           File file = new File(String.format(thumbNailsPath,i+1));
           String base64img = imageService.imageToBase64(file);
           thumbnails.add(new Thumbnail(base64img));
           file.delete();
       }
       return thumbnails;
    }
}
