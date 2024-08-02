package com.example.Angle.Services.Comments;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Comment;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Services.Comments.Interfaces.CommentModeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CommentModerationService implements CommentModeration {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentModerationService(CommentRepository commentRepository){
        this.commentRepository = commentRepository;
    }
    @Override
    public void banComment(String id) throws MediaNotFoundException {
        Comment comment = commentRepository.findById(id).orElse(null);
        if(comment == null){
            throw new MediaNotFoundException("Comment doesn't exists!");
        }
        comment.setBanned(true);
        commentRepository.save(comment);
    }

    @Override
    public void unbanComment(String id) {
        commentRepository.unbanComment(id);

    }
}
