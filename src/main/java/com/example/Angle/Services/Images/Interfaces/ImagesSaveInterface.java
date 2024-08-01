package com.example.Angle.Services.Images.Interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface ImagesSaveInterface {

    void saveImage(String path, String base64Content) throws IOException;

    String saveVideoThumbnail(String base64Content,String videoId) throws IOException;

    String saveUserAvatar(String base64Content, String userId) throws IOException;

}
