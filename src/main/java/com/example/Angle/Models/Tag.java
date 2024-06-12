package com.example.Angle.Models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    private String name;


    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    private Set<Video> videos = new HashSet<>();

}
