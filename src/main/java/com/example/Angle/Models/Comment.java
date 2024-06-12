package com.example.Angle.Models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "authorid")
    private UUID authorId;

    @Column(name = "videoid")
    private UUID videoId;

    @Column(name = "datepublished")
    private Date datePublished;

    @Transient
    private String authorName;

    @Transient
    private String authorAvatar;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int likes;

    private int dislikes;




}
