package com.example.Angle.Services.Images;

import com.example.Angle.Services.Images.Interfaces.ImagesConverterInterface;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Service
public class ImageConverterService implements ImagesConverterInterface {

    public String convertAvatarToBase64(MultipartFile file) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(150,150)
                .outputQuality(0.8)
                .toOutputStream(byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(imgBytes);
    }
    @Override
    public String convertToBase64(File file) throws IOException {
        byte[] imgBytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(imgBytes);
    }
    @Override
    public String convertToBase64(MultipartFile file) throws IOException {
        byte[] imgBytes = file.getBytes();
        return Base64.getEncoder().encodeToString(imgBytes);
    }
}
