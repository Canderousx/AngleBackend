package com.example.Angle.Config.SecServices;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnvironmentVariables {

    private String secretKey;

    private String ffmpegPath;

    private String hlsOutputPath;

    private String thumbnailsPath;

    private String ffmpegTempFolder;

    public boolean checkIfNotNull(){
        return  secretKey != null &&
                ffmpegPath != null &&
                hlsOutputPath != null &&
                thumbnailsPath != null &&
                ffmpegTempFolder !=null;
    }

}
