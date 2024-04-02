package com.example.Angle.Models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    @Column(name = "datepublished")
    private String datePublished;

    @ElementCollection
    private List<String> tags = new ArrayList<>();

    private int views;

    private int likes;

    private int dislikes;

    @Column(name = "authorid")
    private UUID authorId;

    private String video;

    private String thumbnail;

    @Column(name = "isbanned")
    private boolean isBanned;


}
