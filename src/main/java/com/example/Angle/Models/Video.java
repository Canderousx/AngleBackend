package com.example.Angle.Models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    @Column(name = "datepublished")
    private Date datePublished;

    @ManyToMany(cascade =CascadeType.ALL)
    @JoinTable(name ="video_tags",
    joinColumns = @JoinColumn(name = "video_id",referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id",
    referencedColumnName = "id"))
    private Set<Tag> tags;

    private String description;

    private int views = 0;

    private int likes = 0;

    private int dislikes = 0;

    @Column(name = "authorid")
    private UUID authorId;

    private String rawPath;

    private String hlsPath;

    private String thumbnail;

    private String authorAvatar;

    @Column(name = "isbanned")
    private boolean isBanned = false;


}
