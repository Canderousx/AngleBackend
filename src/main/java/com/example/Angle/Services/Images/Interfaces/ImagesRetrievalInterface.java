package com.example.Angle.Services.Images.Interfaces;

import com.example.Angle.Models.Thumbnail;

import java.io.IOException;

public interface ImagesRetrievalInterface {

    Thumbnail getImage(String path) throws IOException, ClassNotFoundException;



}
