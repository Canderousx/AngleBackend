package com.example.Angle.Repositories;


import com.example.Angle.Models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TagRepository extends JpaRepository<Tag,Long> {

    Optional<Tag>findByName(String name);

    boolean existsByName(String name);


    @Query(value = "SELECT t FROM Tag t JOIN t.videos v WHERE v.id = :videoId",nativeQuery = true)
    List<Tag>findTagsByVideoId(@Param("videoId")String videoId);

    @Query(value = "SELECT DISTINCT LOWER(t.name) FROM tag t WHERE LOWER(t.name) LIKE CONCAT(:tagName, '%') ORDER BY LOWER(t.name) ASC", nativeQuery = true)
    List<String>findNameContaining(@Param("tagName")String tagName);


}
