package com.example.Angle.Services.Comments;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Models.Comment;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Services.Comments.Interfaces.CommentRetrieval;
import com.example.Angle.Services.Images.ImageRetrievalService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class CommentRetrievalService implements CommentRetrieval {

    private final CommentRepository commentRepository;

    private final AccountRetrievalService accountRetrievalService;

    private final ImageRetrievalService imageRetrievalService;

    private final Logger log = LogManager.getLogger(CommentManagementService.class);


    @Autowired
    public CommentRetrievalService(CommentRepository commentRepository,
                                   ImageRetrievalService imageRetrievalService,
                                   AccountRetrievalService accountRetrievalService){
        this.commentRepository = commentRepository;
        this.imageRetrievalService = imageRetrievalService;
        this.accountRetrievalService = accountRetrievalService;
    }
    @Override
    public Comment getComment(String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new MediaNotFoundException("Requested comment doesn't exist!" ));
        return fillCommentData(comment);
    }

    @Override
    public List<Comment> getUserComments(String userId) {
        List<Comment> userComments = this.commentRepository.findByAuthorId(userId);
        if(!userComments.isEmpty()){
            log.info("User ["+userId+"] comments found");
            return new ArrayList<>(userComments.stream().toList());
        }else{
            log.info("User ["+userId+"] doesn't have any comments");
            return new ArrayList<>();
        }
    }

    @Override
    public Page<Comment> getVideoComments(String videoId, Pageable pageable) throws IOException, ClassNotFoundException, MediaNotFoundException {
        Page<Comment> pageComments = this.commentRepository.findByVideoId(videoId,pageable);
        if(!pageComments.isEmpty()){
            log.info("Video ["+videoId+"] comments found");
            return fillCommentData(pageComments);
        }else{
            log.info("Video ["+videoId+"] doesn't have any comments yet");
            return Page.empty();
        }
    }

    @Override
    public int getTotalCommentsNum(String videoId) {
        int size = commentRepository.countAllVideoComments(videoId);
        log.info("Total Comments for video {"+videoId+"}: "+size);
        return size;
    }


    private Page<Comment>fillCommentData(Page<Comment> comments) throws IOException, ClassNotFoundException, MediaNotFoundException {
        for(Comment comment: comments.getContent()){
            fillCommentData(comment);
        }
        return comments;
    }

    private Comment fillCommentData(Comment comment) throws IOException, ClassNotFoundException, MediaNotFoundException {
        Account account = accountRetrievalService.getUser(comment.getAuthorId());
        if(account!=null){
            comment.setAuthorAvatar(imageRetrievalService.getImage(account.getAvatar()).getContent());
            comment.setAuthorName(account.getUsername());
        }else{
            comment.setAuthorName("Account disabled");
        }
        return comment;
    }
}
