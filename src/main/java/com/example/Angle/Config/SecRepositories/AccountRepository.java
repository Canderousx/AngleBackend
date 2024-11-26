package com.example.Angle.Config.SecRepositories;


import com.example.Angle.Config.Models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account>findByUsername(String username);

    Optional<Account>findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<Account> findByActive(boolean active);

    @Query(value = "SELECT * FROM Account a JOIN account_liked_videos alv ON a.id = alv.account_id WHERE alv.liked_videos = :videoId", nativeQuery = true)
    List<Account> findUsersWhoLikeVideo(@Param("videoId") String videoId);

    @Query(value = "SELECT * FROM Account a JOIN account_disliked_videos alv ON a.id = alv.account_id WHERE alv.disliked_videos = :videoId", nativeQuery = true)
    List<Account> findUsersWhoDislikeVideo(@Param("videoId") String videoId);

    @Query(value = "SELECT a.* FROM Account a JOIN Report r ON a.id = r.reporter_id WHERE r.id = :reportId",nativeQuery = true)
    List<Account> findReportAuthor(@Param("reportId")String reportId);

    @Query(value = "SELECT a.* FROM Account a JOIN Video v ON a.id = v.authorid WHERE v.id = :mediaId",nativeQuery = true)
    List<Account> findVideoAuthor(@Param("mediaId")String mediaId);

    @Query(value = "SELECT a.* FROM Account a JOIN Comment c ON a.id = c.authorid WHERE c.id = :mediaId",nativeQuery = true)
    List<Account> findCommentAuthor(@Param("mediaId")String mediaId);

    @Query(value = "SELECT a.active FROM Account a WHERE a.email = :email",nativeQuery = true)
    boolean isActive(@Param("email")String email);



}
