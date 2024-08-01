package com.example.Angle.Services.Comments.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;

public interface CommentModeration {

    void banComment(String id) throws MediaNotFoundException;

    void unbanComment(String id);
}
