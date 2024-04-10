package com.cumulations.music_player.dto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "musics")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "location")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String location;

    @Lob
    @Column(name = "metadata",columnDefinition = "JSON")
    private Object metaData;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "album_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonBackReference
    private Album album;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name="creator_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserInfo creator;

    public Music() {
    }

    public Music(String title, String location, Object metaData, Album album, UserInfo creator) {
        this.title = title;
        this.location = location;
        this.metaData = metaData;
        this.album = album;
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Object getMetaData() {
        return metaData;
    }

    public void setMetaData(Object metaData) {
        this.metaData = metaData;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public void setCreator(UserInfo creator) {
        this.creator = creator;
    }
}
