package com.example.Angle.Services.Files.Interfaces;

import com.example.Angle.Config.Exceptions.FileStoreException;
import org.springframework.web.multipart.MultipartFile;

public interface FilesSaveInterface {

    String saveRawFile(MultipartFile file) throws FileStoreException;
}
