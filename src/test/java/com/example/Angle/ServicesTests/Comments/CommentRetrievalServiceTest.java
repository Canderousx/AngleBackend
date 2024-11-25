package com.example.Angle.ServicesTests.Comments;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Models.Comment;
import com.example.Angle.Models.Thumbnail;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Services.Comments.CommentRetrievalService;
import com.example.Angle.Services.Images.ImageRetrievalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentRetrievalServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AccountRetrievalService accountRetrievalService;

    @Mock
    private ImageRetrievalService imageRetrievalService;

    @InjectMocks
    private CommentRetrievalService commentRetrievalService;

    private final Comment comment = new Comment();

    @Test
    void getComment_CommentExist(){
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        assertDoesNotThrow(() -> commentRetrievalService.getComment(comment.getId()));

        verify(commentRepository,times(1)).findById(comment.getId());
    }

    @Test
    void getComment_CommentNotExist(){
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.empty());
        assertThrows(MediaNotFoundException.class,() -> commentRetrievalService.getComment(comment.getId()));
        verify(commentRepository,times(1)).findById(comment.getId());
    }

    @Test
    void getUserComments_CommentsExist(){
        List<Comment> comments = Arrays.asList(comment,comment,comment);
        when(commentRepository.findByAuthorId(comment.getAuthorId())).thenReturn(comments);

        List<Comment> result = commentRetrievalService.getUserComments(comment.getAuthorId());

        assertEquals(3,result.size());

        verify(commentRepository,times(1)).findByAuthorId(comment.getAuthorId());
    }

    @Test
    void getUserComments_CommentsNotExist(){
        when(commentRepository.findByAuthorId(comment.getAuthorId())).thenReturn(new ArrayList<>());

        List<Comment> result = commentRetrievalService.getUserComments(comment.getAuthorId());

        assertEquals(0,result.size());

        verify(commentRepository,times(1)).findByAuthorId(comment.getAuthorId());
    }


    @Test
    void getVideoCommentsTest_CommentsExist() throws MediaNotFoundException, IOException, ClassNotFoundException {
        List<Comment> comments = Arrays.asList(comment,comment,comment);
        Pageable pageable = PageRequest.of(0,10);
        Page<Comment> page = new PageImpl<>(comments);
        Account account = new Account();
        account.setUsername("User1");
        account.setId("User1");
        account.setAvatar("avatar1.png");


        when(commentRepository.findByVideoId(comment.getVideoId(), pageable)).thenReturn(page);
        when(accountRetrievalService.getUser(null)).thenReturn(account);
        when(imageRetrievalService.getImage("avatar1.png")).thenReturn(new Thumbnail("xddd"));

        Page<Comment> result = commentRetrievalService.getVideoComments(
                comment.getVideoId(),
                pageable
        );
        assertNotNull(result);
        assertEquals(3,result.getContent().size());
        assertNull(result.getContent().get(1).getAuthorId());
        assertNotNull(result.getContent().get(0).getAuthorAvatar());
        verify(commentRepository,times(1)).findByVideoId(comment.getVideoId(),pageable);
    }

    @Test
    void getVideoCommentsTest_NoComments() throws IOException, ClassNotFoundException, MediaNotFoundException {
        Pageable pageable = PageRequest.of(0,10);
        Page<Comment> page = new PageImpl<>(new ArrayList<>());
        Account account = new Account();
        account.setUsername("User1");
        account.setId("User1");
        account.setAvatar("avatar1.png");

        when(commentRepository.findByVideoId(comment.getVideoId(), pageable)).thenReturn(page);

        Page<Comment> result = commentRetrievalService.getVideoComments(
                comment.getVideoId(),
                pageable
        );
        assertNotNull(result);
        assertEquals(0,result.getContent().size());
        verify(commentRepository,times(1)).findByVideoId(comment.getVideoId(),pageable);
    }










}
