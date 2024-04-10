package com.cumulations.music_player.repo;

import com.cumulations.music_player.dto.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicRepo extends JpaRepository<Music,String> {
}
