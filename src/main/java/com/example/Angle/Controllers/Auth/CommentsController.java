package com.example.Angle.Controllers.Auth;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.Comment;
import com.example.Angle.Models.ReportCategories;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Services.CommentService;
import com.example.Angle.Services.ImageService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping(value = "/auth/comments")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
public class CommentsController {

    private final Logger logger = LogManager.getLogger(CommentsController.class);

    @Autowired
    CommentService commentService;

    @RequestMapping(value = "/getComment",method = RequestMethod.GET)
    public Comment getComment(@RequestParam String id) throws IOException, ClassNotFoundException, MediaNotFoundException {
        return commentService.getComment(id);
    }


    @RequestMapping(value = "/addComment",method = RequestMethod.POST)
    public List<Comment> addComment(@RequestBody Comment comment,
                                    HttpServletResponse response) throws IOException, ClassNotFoundException, MediaNotFoundException {
        comment.setDatePublished(new Date());
        commentService.addComment(comment);
        Pageable paginateSettings = PageRequest.of(0,10, Sort.by("datePublished").descending());
        List<Comment> refreshed = commentService.getVideoComments(comment.getVideoId(),paginateSettings);
        response.setHeader("totalComments",String.valueOf(refreshed.size()));
        return refreshed;
    }

    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    public ResponseEntity<SimpleResponse>deleteComment(@RequestParam String id) throws IOException, MediaNotFoundException, ClassNotFoundException {
        commentService.removeComment(id);
        return ResponseEntity.ok(new SimpleResponse("The comment has been deleted."));
    }
}
