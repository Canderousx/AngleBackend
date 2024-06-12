package com.example.Angle.Services;


import com.example.Angle.Models.Comment;
import com.example.Angle.Repositories.CommentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public boolean removeVideoComments(UUID videoId){
        List<Comment>comments = commentRepository.findByVideoId(videoId).stream().toList();
        if(comments.isEmpty()){
            log.info("Requested video doesn't have any comments.");
            return false;
        }
        comments.forEach(comment ->{
            log.info("Deleting comment ID: "+comment.getId());
            commentRepository.delete(comment);
        });
        return true;

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

    public String getTotalCommentsNum(UUID videoId){
        String size = String.valueOf(commentRepository.findByVideoId(videoId).size());
        log.info("Total comments: "+size);
        return size;
    }

    public List<Comment> getVideoComments(UUID videoId, Pageable pageable){
        Page<Comment> pageComments = this.commentRepository.findByVideoId(videoId,pageable);
        if(!pageComments.isEmpty()){
            log.info("Video ["+videoId+"] comments found");
            return new ArrayList<>(pageComments.stream().toList());
        }else{
            log.info("Video ["+videoId+"] doesn't have any comments yet");
            return new ArrayList<>();
        }
    }
}
