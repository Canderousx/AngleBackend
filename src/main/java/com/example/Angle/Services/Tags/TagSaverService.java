package com.example.Angle.Services.Tags;

import com.example.Angle.Models.Tag;
import com.example.Angle.Models.Video;
import com.example.Angle.Repositories.TagRepository;
import com.example.Angle.Services.Tags.Interface.TagSaverInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TagSaverService implements TagSaverInterface {

    private TagRepository tagRepository;

    private final Logger logger = LogManager.getLogger(TagSaverService.class);


    @Autowired
    public TagSaverService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    @Override
    public Set<Tag> setTags(Video metaData) {
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
