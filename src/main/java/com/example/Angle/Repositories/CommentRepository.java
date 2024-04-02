package com.example.Angle.Repositories;


import com.example.Angle.Models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Optional<Comment>findByAuthorId(UUID authorId);

    Optional<Comment>findByVideoId(UUID videoId);

}
