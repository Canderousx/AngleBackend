package com.example.Angle.Repositories;


import com.example.Angle.Models.Video;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {

    Optional<Video>findByName(String name);

    Optional<Video>findByDatePublished(Date datePublished);

    List<Video> findAllByThumbnailIsNotNullAndNameIsNotNull(Pageable page);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM video_tags WHERE video_id = :videoId", nativeQuery = true)
    void deleteTagAssociations(@Param("videoId") UUID videoId);


    @Query(value = "SELECT * FROM Video v WHERE FIND_IN_SET(:tag, v.tags) > 0 AND v.thumbnail IS NOT NULL AND v.name IS NOT NULL", nativeQuery = true)
    Optional<Video>findByTag(String tag);

    List<Video>findByAuthorId(UUID authorId,Pageable pageable);

    List<Video>findByAuthorId(UUID authorId);

    @Query(value = "SELECT v.* FROM video v JOIN video_tags vt ON v.id = vt.video_id JOIN tag t ON t.id = vt.tag_id WHERE t.name IN :tagNames AND v.id != :currentVideoId AND v.thumbnail IS NOT NULL AND v.name IS NOT NULL GROUP BY v.id ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Video> findSimilar(@Param("tagNames")Set<String>tagNames, @Param("currentVideoId") UUID videoId);


    @Query(value = "SELECT v.* FROM video v WHERE v.id NOT IN (:videoIds) AND v.thumbnail IS NOT NULL AND v.name IS NOT NULL AND v.id != :currentVideoId ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Video>findRandom(@Param("videoIds")List<UUID> videoId,@Param ("currentVideoId") UUID currentId, @Param("limit")int howMany);


    @Query(value = "SELECT * FROM video v ORDER BY views DESC LIMIT 4",nativeQuery = true)
    List<Video>findMostPopular();



    @Query(value = "SELECT v.* FROM video v JOIN video_tags vt ON v.id = vt.video_id JOIN tag t ON t.id = vt.tag_id WHERE t.name IN :tagNames AND v.thumbnail IS NOT NULL GROUP BY v.id ORDER BY RAND() LIMIT 8",nativeQuery = true)
    List<Video>findRecommended(@Param("tagNames")Set<String>tagNames);


    List<Video>findByNameContaining(String name);

    @Query(value = "SELECT DISTINCT v.name FROM video v WHERE v.name LIKE :vName% ORDER BY v.name ASC",nativeQuery = true)
    List<String>findNameContaining(@Param("vName")String vName);

    @Query(value = "SELECT DISTINCT v.* FROM video v LEFT JOIN video_tags vt ON v.id = vt.video_id LEFT JOIN tag t ON vt.tag_id = t.id JOIN account a ON v.authorId = a.id WHERE v.name LIKE %:name% OR t.name LIKE %:tagName% OR a.username LIKE %:authorName%",nativeQuery = true)
    Page<Video> findByNameContainingOrTagsNameContaining(@Param("name") String name, @Param("tagName") String tagName,@Param("authorName") String authorName, Pageable pageable);












}
