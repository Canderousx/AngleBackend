package com.example.Angle.Config.SecRepositories;


import com.example.Angle.Config.Models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account>findByUsername(String username);

    Optional<Account>findByEmail(String email);

    List<Account> findByActive(boolean active);

    @Query(value = "SELECT * FROM Account a JOIN account_liked_videos alv ON a.id = alv.account_id WHERE alv.liked_videos = :videoId", nativeQuery = true)
    List<Account> findUsersWhoLikeVideo(@Param("videoId") UUID videoId);

    @Query(value = "SELECT * FROM Account a JOIN account_disliked_videos alv ON a.id = alv.account_id WHERE alv.disliked_videos = :videoId", nativeQuery = true)
    List<Account> findUsersWhoDislikeVideo(@Param("videoId") UUID videoId);



}
