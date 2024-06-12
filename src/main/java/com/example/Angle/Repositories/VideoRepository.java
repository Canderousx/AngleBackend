package com.example.Angle.Repositories;


import com.example.Angle.Models.Video;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {

    Optional<Video>findByName(String name);

    Optional<Video>findByDatePublished(Date datePublished);

    List<Video> findAllByThumbnailIsNotNullAndNameIsNotNullAndIsBannedFalse(Pageable page);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM video_tags WHERE video_id = :videoId", nativeQuery = true)
    void deleteTagAssociations(@Param("videoId") String videoId);


    @Query(value = "SELECT * FROM Video v WHERE FIND_IN_SET(:tag, v.tags) > 0 AND v.thumbnail IS NOT NULL AND v.name IS NOT NULL AND v.isbanned = false", nativeQuery = true)
    Optional<Video>findByTag(String tag);

    List<Video>findByAuthorId(String authorId,Pageable pageable);

    List<Video>findByAuthorId(String authorId);

    @Query(value = "SELECT v.* FROM video v JOIN video_tags vt ON v.id = vt.video_id JOIN tag t ON t.id = vt.tag_id WHERE v.isbanned = false AND t.name IN :tagNames AND v.id != :currentVideoId AND v.thumbnail IS NOT NULL AND v.name IS NOT NULL GROUP BY v.id ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Video> findSimilar(@Param("tagNames")Set<String>tagNames, @Param("currentVideoId") String videoId);


    @Query(value = "SELECT v.* FROM video v WHERE v.isbanned = false AND v.id NOT IN (:videoIds) AND v.thumbnail IS NOT NULL AND v.name IS NOT NULL AND v.id != :currentVideoId ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Video>findRandom(@Param("videoIds")List<String> videoId,@Param ("currentVideoId") String currentId, @Param("limit")int howMany);


    @Query(value = "SELECT * FROM video v WHERE v.isbanned = false ORDER BY views DESC LIMIT 4",nativeQuery = true)
    List<Video>findMostPopular();



    @Query(value = "SELECT v.* FROM video v JOIN video_tags vt ON v.id = vt.video_id JOIN tag t ON t.id = vt.tag_id WHERE v.isbanned = false AND t.name IN :tagNames AND v.thumbnail IS NOT NULL GROUP BY v.id ORDER BY RAND() LIMIT 8",nativeQuery = true)
    List<Video>findRecommended(@Param("tagNames")Set<String>tagNames);


    List<Video>findByNameContaining(String name);

    @Query(value = "SELECT DISTINCT LOWER(v.name) FROM video v WHERE v.isbanned = false AND LOWER(v.name) LIKE CONCAT(:vName, '%') ORDER BY LOWER(v.name) ASC", nativeQuery = true)
    List<String>findNameContaining(@Param("vName")String vName);

    @Query(value = "SELECT DISTINCT v.* FROM video v LEFT JOIN video_tags vt ON v.id = vt.video_id LEFT JOIN tag t ON vt.tag_id = t.id JOIN account a ON v.authorId = a.id WHERE v.isbanned = false AND v.name LIKE %:name% OR t.name LIKE %:tagName% OR a.username LIKE %:authorName% ",nativeQuery = true)
    Page<Video> findByNameContainingOrTagsNameContaining(@Param("name") String name, @Param("tagName") String tagName,@Param("authorName") String authorName, Pageable pageable);



    @Query(value = "SELECT v.* FROM video v JOIN account ON v.authorId = account.id WHERE account.id IN (:subIds) AND v.isbanned = false ORDER BY v.datePublished DESC",
            countQuery = "SELECT count(*) FROM video v JOIN account ON v.authorId = account.id WHERE account.id IN (:subIds)", nativeQuery = true)
    Page<Video> findFromSubscribers(@Param("subIds")String[] subIds,Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "UPDATE video SET isbanned = true WHERE authorid = :accountId",nativeQuery = true)
    void banAllUserVideos(@RequestParam("accountId")String accountId);


    @Modifying
    @Transactional
    @Query(value = "UPDATE video SET isbanned = false WHERE id = :videoId",nativeQuery = true)
    void unbanVideo(@RequestParam("videoId")String videoId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE video SET isbanned = false WHERE authorid = :accountId",nativeQuery = true)
    void unbanAllUserVideos(@RequestParam("accountId")String accountId);

    @Query(value = "SELECT count(*) FROM video v WHERE v.authorId = :accountId",nativeQuery = true)
    int countUserVideos(@Param("accountId")String accountId);

















}
