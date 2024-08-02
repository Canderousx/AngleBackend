package com.example.Angle.Services.Comments;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.Comment;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Services.Comments.Interfaces.CommentManagement;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class CommentManagementService implements CommentManagement {


    private final Logger log = LogManager.getLogger(CommentManagementService.class);
    private final CommentRepository commentRepository;

    private final AccountService accountService;

    private final CommentRetrievalService commentRetrievalServiceImpl;

    @Autowired
    public CommentManagementService(CommentRepository commentRepository,
                                    AccountService accountService,
                                    CommentRetrievalService commentRetrievalService){
        this.commentRepository = commentRepository;
        this.accountService = accountService;
        this.commentRetrievalServiceImpl = commentRetrievalService;
    }
    @Override
    public void addComment(Comment comment) throws BadRequestException {
        if(comment !=null){
            this.commentRepository.save(comment);
            log.info("Comment added successfully!");
        }else{
            log.error("Unable to add new comment. It's NULL");
            throw new BadRequestException("Internal Server Error: COMMENT NULL");
        }
    }

    @Override
    public void removeComment(String id) throws MediaNotFoundException, IOException, ClassNotFoundException {
        Account currentAccount = accountService.getCurrentUser();
        Comment toDelete = commentRetrievalServiceImpl.getComment(id);
        if(currentAccount.getId().equals(toDelete.getAuthorId()) || accountService.isAdmin()){
            this.commentRepository.delete(toDelete);
            log.info("Requested comment ["+id+"] has been deleted");
        }else{
            throw new BadRequestException("Unauthorized");
        }
    }

    @Override
    public void removeVideoComments(String videoId) {
        commentRepository.deleteVideoComments(videoId);
    }
}
