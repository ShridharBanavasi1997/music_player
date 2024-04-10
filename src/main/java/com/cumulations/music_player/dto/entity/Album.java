package com.cumulations.music_player.dto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name="albums")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "album", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonBackReference
    private List<Music> musics = new ArrayList<>();


    public Album() {
    }

    public Album(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Music> getMusics() {
        return musics;
    }

    public void setMusics(List<Music> musics) {
        this.musics = musics;
    }

    public void addMusic(Music music) {
        musics.add(music);
    }
}
