package com.example.Angle.Models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import java.util.Date;

@Entity
@Getter
@Setter
public class Comment {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private String id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "authorid", columnDefinition = "VARCHAR(36)")
    private String authorId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "videoid", columnDefinition = "VARCHAR(36)")
    private String videoId;

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

    @Column(name = "isbanned")
    private boolean isBanned = false;




}
