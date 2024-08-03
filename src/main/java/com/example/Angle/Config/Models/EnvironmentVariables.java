package com.example.Angle.Config.Models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnvironmentVariables {

    private String secretKey;

    private String ffmpegPath;

    private String hlsOutputPath;

    private String ffmpegTempThumbnailsPath;

    private String ffmpegTempFolder;

    private String avatarsPath;

    private String thumbnailsPath;

    private String rawFilesPath;

    private String hlsFilesPath;

    private String frontUrl;


    public boolean checkIfNotNull(){
        for(Field field : this.getClass().getDeclaredFields()){
            field.setAccessible(true);
            try {
                if(field.get(this) == null){
                    return false;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error checking null for field "+field.getName(),e);
            }
        }
        return true;
    }

}
