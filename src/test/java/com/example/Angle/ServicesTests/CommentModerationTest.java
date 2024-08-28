package com.example.Angle.ServicesTests;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Comment;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Services.Comments.CommentModerationService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentModerationTest {
    @Mock
    private  CommentRepository commentRepository;
    @InjectMocks
    private CommentModerationService commentModerationService;

    private final Comment comment = new Comment();


    @Test
    void banComment_CommentExists() throws MediaNotFoundException {
        comment.setBanned(false);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        assertDoesNotThrow(() -> commentModerationService.banComment(comment.getId()));
        assertTrue(comment.isBanned());
        verify(commentRepository,times(1)).findById(comment.getId());
    }

    @Test
    void banComment_CommentNotExist() throws MediaNotFoundException {
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.empty());
        assertThrows(MediaNotFoundException.class,() -> commentModerationService.banComment(comment.getId()));
        verify(commentRepository,times(1)).findById(comment.getId());
    }





}
