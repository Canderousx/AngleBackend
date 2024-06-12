package com.example.Angle.Services;


import com.example.Angle.Models.Tag;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.TagRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class TagService {

    @Autowired
    TagRepository tagRepository;

    private final Logger logger = LogManager.getLogger(TagService.class);

    public Set<Tag> setTags(Video metaData){
        Set<Tag> tags = new HashSet<>();
        for(Tag tag: metaData.getTags()){
            if(!tagRepository.existsByName(tag.getName().toLowerCase())){
                logger.info("New tag detected");
                tagRepository.save(tag);
                logger.info("New tag saved!");
            }
            tagRepository.findByName(tag.getName()).ifPresent(tags::add);
        }
        if(tags.isEmpty()){
            logger.error("Warning: Video without any tags detected!");
        }
        return tags;
    }
}
