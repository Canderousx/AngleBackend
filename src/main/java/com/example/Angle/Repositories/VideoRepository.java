package com.example.Angle.Repositories;


import com.example.Angle.Models.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {

    Optional<Video>findByName(String name);

    Optional<Video>findByDatePublished(String datePublished);

    @Query(value = "SELECT * FROM Video v WHERE FIND_IN_SET(:tag, v.tags) > 0", nativeQuery = true)
    Optional<Video>findByTag(String tag);

    Optional<Video>findByAuthorId(UUID authorId);


}
