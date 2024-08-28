package com.example.Angle.ServicesTests;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Models.Comment;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Services.Comments.CommentManagementService;
import com.example.Angle.Services.Comments.CommentRetrievalService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentManagementServiceTest {


    @Mock
    private CommentRepository commentRepository;


    @Mock
    private AccountRetrievalService accountRetrievalService;


    @Mock
    private CommentRetrievalService commentRetrievalServiceImpl;

    @InjectMocks
    private CommentManagementService commentManagementService;



    private final Comment comment = new Comment();

    private Account user;

    @BeforeEach()
    void setUp(){
        user = new Account();
        user.setId("xyz");
    }


    @Test
    void addCommentTest_NotNull(){
        when(commentRepository.save(comment)).thenReturn(comment);
        assertDoesNotThrow(() -> commentManagementService.addComment(comment));
        verify(commentRepository,times(1)).save(comment);
    }

    @Test
    void addCommentTest_Null(){
        Comment nullComment = null;
        assertThrows(BadRequestException.class, () -> commentManagementService.addComment(nullComment));

        verify(commentRepository,times(0)).save(comment);
    }


    @Test
    void removeCommentTest_AuthorizedUser() throws IOException, MediaNotFoundException, ClassNotFoundException {
        this.comment.setAuthorId(user.getId());
        when(accountRetrievalService.getCurrentUser()).thenReturn(user);
        when(commentRetrievalServiceImpl.getComment(comment.getId())).thenReturn(comment);
        assertDoesNotThrow(() -> commentManagementService.removeComment(comment.getId()));

        verify(accountRetrievalService,times(1)).getCurrentUser();
        verify(commentRetrievalServiceImpl,times(1)).getComment(comment.getId());

    }

    @Test
    void removeCommentTest_Admin() throws IOException, MediaNotFoundException, ClassNotFoundException {
        this.comment.setAuthorId(user.getId()+"xxx");
        when(accountRetrievalService.getCurrentUser()).thenReturn(user);
        when(commentRetrievalServiceImpl.getComment(comment.getId())).thenReturn(comment);
        when(accountRetrievalService.isAdmin()).thenReturn(true);

        assertDoesNotThrow(() -> commentManagementService.removeComment(comment.getId()));

        verify(accountRetrievalService,times(1)).getCurrentUser();
        verify(commentRetrievalServiceImpl,times(1)).getComment(comment.getId());
        verify(accountRetrievalService,times(1)).isAdmin();
    }

    @Test
    void removeCommentTest_UnauthorizedUser() throws IOException, MediaNotFoundException, ClassNotFoundException {
        this.comment.setAuthorId(user.getId()+"xxx");
        when(accountRetrievalService.getCurrentUser()).thenReturn(user);
        when(commentRetrievalServiceImpl.getComment(comment.getId())).thenReturn(comment);
        when(accountRetrievalService.isAdmin()).thenReturn(false);

        assertThrows(BadRequestException.class,() -> commentManagementService.removeComment(comment.getId()));
        verify(accountRetrievalService,times(1)).getCurrentUser();
        verify(commentRetrievalServiceImpl,times(1)).getComment(comment.getId());
        verify(accountRetrievalService,times(1)).isAdmin();

    }








}
