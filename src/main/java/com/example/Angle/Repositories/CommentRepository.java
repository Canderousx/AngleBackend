package com.example.Angle.Repositories;


import com.example.Angle.Models.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;


@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    Optional<Comment>findByAuthorId(String authorId);

    Page<Comment> findByVideoId(String videoId, Pageable pageable);

    List<Comment>findByVideoId(String videoId);

    @Query(value = "SELECT count(*) FROM comment c WHERE c.videoid = :videoId",nativeQuery = true)
    int countAllVideoComments(@RequestParam("videoId")String videoId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comment WHERE videoid = :videoId",nativeQuery = true)
    void deleteVideoComments(@RequestParam("videoId")String videoId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE comment SET isbanned = true WHERE authorid = :accountId",nativeQuery = true)
    void banAllUserComments(@RequestParam("accountId")String accountId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE comment SET isbanned = true WHERE id = :commentId",nativeQuery = true)
    void banComment(@RequestParam("commentId")String commentId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE comment SET isbanned = false WHERE id = :commentId",nativeQuery = true)
    void unbanComment(@RequestParam("commentId")String commentId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE comment SET isbanned = false WHERE authorid = :accountId",nativeQuery = true)
    void unbanAllUserComments(@RequestParam("accountId")String accountId);



}
