package com.example.Angle.Services.FFMpeg;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.EnvironmentVariables;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Models.Video;
import com.example.Angle.Services.FFMpeg.Interfaces.FFMpegDataRetrievalInterface;
import com.example.Angle.Services.Images.ImageConverterService;
import com.example.Angle.Services.Videos.VideoRetrievalService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FFMpegDataRetrievalService implements FFMpegDataRetrievalInterface {

    private final Logger logger = LogManager.getLogger(FFMpegDataRetrievalService.class);

    private final EnvironmentVariables environmentVariables;

    private final ImageConverterService imageConverterService;

    private final VideoRetrievalService videoRetrievalService;

    private final AccountRetrievalService accountRetrievalService;


    @Autowired
    public FFMpegDataRetrievalService(EnvironmentVariables environmentVariables, ImageConverterService imageConverterService, VideoRetrievalService videoRetrievalService, AccountRetrievalService accountRetrievalService) {
        this.environmentVariables = environmentVariables;
        this.imageConverterService = imageConverterService;
        this.videoRetrievalService = videoRetrievalService;
        this.accountRetrievalService = accountRetrievalService;
    }

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

    @Override
    public double getVideoDuration(String rawPath) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries",
                "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", rawPath);
        Process process = builder.start();
        String output = inputStreamToString(process.getInputStream());
        logger.info("Video duration: "+output);
        process.waitFor();
        return Double.parseDouble(output.trim());
    }

    @Override
    public List<Thumbnail> getVideoThumbnails(String vId) throws MediaNotFoundException, IOException, ClassNotFoundException, InterruptedException {
        Video video = videoRetrievalService.getVideo(vId);
        Account account = accountRetrievalService.getCurrentUser();
        if(!account.getId().equals(video.getAuthorId())){
            throw new BadCredentialsException("Unauthorized");
        }
        return generateVideoThumbnails(video.getRawPath());
    }

    @Override
    public List<Thumbnail> generateVideoThumbnails(String rawPath) throws IOException, InterruptedException {
        double videoLength = getVideoDuration(rawPath);
        File rawFile = new File(rawPath);
        Path path = rawFile.toPath();

        String processedPath = path.toString();
        System.out.println("PROCESSED PATH: "+processedPath);
        int framesNumber = (int) Math.floor(videoLength / 2);
        if (framesNumber > 5) {
            framesNumber = 5;
        } else if (framesNumber < 1) {
            framesNumber = 1;
        }



        List<String> command = Arrays.asList(
                environmentVariables.getFfmpegPath(), "-i", rawPath,
                "-vf", "fps=" + (videoLength < 2 ? "1" : "1/2") + ",scale=320:-1",
                "-vframes", String.valueOf(framesNumber),
                "-compression_level", "6", "-preset", "photo",
                "-f", "image2",
                environmentVariables.getFfmpegTempThumbnailsPath()+"/thumbnail%03d.png"
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
            File file = new File(String.format(environmentVariables.getFfmpegTempThumbnailsPath(),i+1));
            String base64img = imageConverterService.convertToBase64(file);
            thumbnails.add(new Thumbnail(base64img));
            file.delete();
        }
        return thumbnails;
    }
}
