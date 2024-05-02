package com.example.Angle.Models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VideoDetails {
    private String name;
    private String description;
    private List<String> tags = new ArrayList<>();

    private String thumbnail;
}
