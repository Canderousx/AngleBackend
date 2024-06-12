package com.example.Angle.Services;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.Comment;
import com.example.Angle.Repositories.CommentRepository;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ImageService imageService;

    private final Logger log = LogManager.getLogger(CommentService.class);

    public void addComment(Comment comment) throws BadRequestException {
        if(comment !=null){
            this.commentRepository.save(comment);
            log.info("Comment added successfully!");
        }else{
            log.error("Unable to add new comment. It's NULL");
            throw new BadRequestException("Internal Server Error: COMMENT NULL");
        }
    }

    public Comment getComment(String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
        Comment comment = commentRepository.findById(id).orElse(null);
        if(comment == null){
            log.error("Comment id: {"+id+"} NOT FOUND!");
            throw new MediaNotFoundException("Requested comment doesn't exist!");
        }
        return fillCommentData(comment);
    }

    public Comment getRawComment(String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
        Comment comment = commentRepository.findById(id).orElse(null);
        if(comment == null){
            log.error("Comment id: {"+id+"} NOT FOUND!");
            throw new MediaNotFoundException("Requested comment doesn't exist!");
        }
        return comment;
    }



    public void banComment(String id) throws MediaNotFoundException {
        Comment comment = commentRepository.findById(id).orElse(null);
        if(comment == null){
            throw new MediaNotFoundException("Comment doesn't exists!");
        }
        comment.setBanned(true);
        commentRepository.save(comment);
    }

    public void unbanComment(String commentId){
        commentRepository.unbanComment(commentId);
    }

    public void removeComment(String commentId) throws MediaNotFoundException, IOException, ClassNotFoundException {
        Account currentAccount = accountService.getCurrentUser();
        Comment toDelete = this.getComment(commentId);
        if(currentAccount.getId().equals(toDelete.getAuthorId()) || accountService.isAdmin()){
            this.commentRepository.delete(toDelete);
            log.info("Requested comment ["+commentId+"] has been deleted");
        }else{
            throw new BadRequestException("Unauthorized");
        }
    }


    public List<Comment>fillCommentData(List<Comment> comments) throws IOException, ClassNotFoundException, MediaNotFoundException {
        List<Comment>filled = new ArrayList<>();
        for(Comment comment: comments){
            filled.add(fillCommentData(comment));
        }
        return filled;
    }

    public Comment fillCommentData(Comment comment) throws IOException, ClassNotFoundException, MediaNotFoundException {
        Account account = accountService.getUser(comment.getAuthorId());
        if(account!=null){
            comment.setAuthorAvatar(imageService.readImage(account.getAvatar()).getContent());
            comment.setAuthorName(account.getUsername());
        }else{
            comment.setAuthorName("Account disabled");
        }
        return comment;
    }

    public boolean removeVideoComments(String videoId){
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

    public List<Comment> getUserComments(String userId){
        Optional<Comment> userComments = this.commentRepository.findByAuthorId(userId);
        if(userComments.isPresent()){
            log.info("User ["+userId+"] comments found");
            return new ArrayList<>(userComments.stream().toList());
        }else{
            log.info("User ["+userId+"] doesn't have any comments");
            return new ArrayList<>();
        }
    }

    public String getTotalCommentsNum(String videoId){
        String size = String.valueOf(commentRepository.findByVideoId(videoId).size());
        log.info("Total comments: "+size);
        return size;
    }

    public List<Comment> getVideoComments(String videoId, Pageable pageable) throws IOException, ClassNotFoundException, MediaNotFoundException {
        Page<Comment> pageComments = this.commentRepository.findByVideoId(videoId,pageable);
        if(!pageComments.isEmpty()){
            log.info("Video ["+videoId+"] comments found");
            return fillCommentData(pageComments.stream().toList());
        }else{
            log.info("Video ["+videoId+"] doesn't have any comments yet");
            return new ArrayList<>();
        }
    }
}
