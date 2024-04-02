package com.example.Angle.Services;


import com.example.Angle.Models.Comment;
import com.example.Angle.Repositories.CommentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    private final Logger log = LogManager.getLogger(CommentService.class);

    public boolean addComment(Comment comment){
        if(comment !=null){
            this.commentRepository.save(comment);
            log.info("Comment added successfully!");
            return true;
        }else{
            log.error("Unable to add new comment. It's NULL");
            return false;
        }
    }

    public boolean removeComment(UUID commentId){
        Comment toDelete = this.commentRepository.findById(commentId).orElse(null);
        if(toDelete!=null){
            this.commentRepository.delete(toDelete);
            log.info("Requested comment ["+commentId+"] has been deleted");
            return true;
        }else{
            log.error("Couldn't delete requested comment ["+commentId+"]. NOT FOUND");
            return false;
        }
    }

    public List<Comment> getUserComments(UUID userId){
        Optional<Comment> userComments = this.commentRepository.findByAuthorId(userId);
        if(userComments.isPresent()){
            log.info("User ["+userId+"] comments found");
            return new ArrayList<>(userComments.stream().toList());
        }else{
            log.info("User ["+userId+"] doesn't have any comments");
            return new ArrayList<>();
        }
    }

    public List<Comment> getVideoComments(UUID videoId){
        Optional<Comment> videoComments = this.commentRepository.findByVideoId(videoId);
        if(videoComments.isPresent()){
            log.info("Video ["+videoId+"] comments found");
            return new ArrayList<>(videoComments.stream().toList());
        }else{
            log.info("Video ["+videoId+"] doesn't have any comments yet");
            return new ArrayList<>();
        }
    }
}
