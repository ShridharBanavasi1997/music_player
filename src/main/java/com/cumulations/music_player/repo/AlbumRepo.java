package com.cumulations.music_player.repo;

import com.cumulations.music_player.dto.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface AlbumRepo extends JpaRepository<Album,Integer> {
//    @Query("SELECT a FROM Album a JOIN FETCH a.musics s GROUP BY a.id ORDER BY COUNT(s.id) DESC")
//    List<Album> findAllWithSongsOrderedBySongCountDesc();

    //Write ROM for getting albumwith most songs
    @Query("SELECT a FROM Album a JOIN FETCH a.musics m GROUP BY a.id, a.name ORDER BY COUNT(m.id) DESC")
    List<Album> findAllWithSongsOrderedBySongCountDesc();
}
