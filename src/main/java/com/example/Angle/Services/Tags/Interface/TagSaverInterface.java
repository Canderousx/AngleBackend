package com.example.Angle.Services.Tags.Interface;

import com.example.Angle.Models.Tag;
import com.example.Angle.Models.Video;

import java.util.Set;

public interface TagSaverInterface {


    Set<Tag> setTags(Video metaData);
}
