package com.example.Angle.Controllers;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Models.Comment;
import com.example.Angle.Services.Comments.CommentManagementService;
import com.example.Angle.Services.Comments.CommentRetrievalService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/comments")
public class CommentController {
    private final CommentRetrievalService commentRetrievalService;

    private final CommentManagementService commentManagementService;

    @Autowired
    public CommentController(CommentRetrievalService commentRetrievalService,
                             CommentManagementService commentManagementService){
        this.commentRetrievalService = commentRetrievalService;
        this.commentManagementService = commentManagementService;
    }

    @RequestMapping(value = "/getComment",method = RequestMethod.GET)
    public Comment getComment(@RequestParam String id) throws IOException, ClassNotFoundException, MediaNotFoundException {
        return commentRetrievalService.getComment(id);
    }


    @RequestMapping(value = "/addComment",method = RequestMethod.POST)
    public Page<Comment> addComment(@RequestBody Comment comment,
                                    HttpServletResponse response) throws IOException, ClassNotFoundException, MediaNotFoundException {
        commentManagementService.addComment(comment);
        Page<Comment> refreshed = commentRetrievalService.getVideoComments(comment.getVideoId(),0,10);
        response.setHeader("totalComments",String.valueOf(refreshed.getTotalElements()));
        return refreshed;
    }

    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    public ResponseEntity<SimpleResponse>deleteComment(@RequestParam String id) throws IOException, MediaNotFoundException, ClassNotFoundException {
        commentManagementService.removeComment(id);
        return ResponseEntity.ok(new SimpleResponse("The comment has been deleted."));
    }
}
