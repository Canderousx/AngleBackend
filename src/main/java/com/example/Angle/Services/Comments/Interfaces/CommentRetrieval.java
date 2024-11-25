package com.example.Angle.Services.Comments.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface CommentRetrieval {

    Comment getComment(String id) throws MediaNotFoundException, IOException, ClassNotFoundException;

    List<Comment> getUserComments(String userId);

    Page<Comment> getVideoComments(String videoId, Pageable pageable) throws IOException, ClassNotFoundException, MediaNotFoundException;

    int getTotalCommentsNum(String videoId);




}
