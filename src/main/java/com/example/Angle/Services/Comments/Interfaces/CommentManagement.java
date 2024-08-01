package com.example.Angle.Services.Comments.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Comment;
import org.apache.coyote.BadRequestException;

import java.io.IOException;

public interface CommentManagement {

    void addComment(Comment comment) throws BadRequestException;

    void removeComment(String id) throws MediaNotFoundException, IOException, ClassNotFoundException;

    void removeVideoComments(String videoId);

}
