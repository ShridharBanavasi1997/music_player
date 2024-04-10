package com.cumulations.music_player.repo;

import com.cumulations.music_player.dto.entity.RefreshToken;
import com.cumulations.music_player.dto.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUserInfo(UserInfo user);

    Boolean existsByUserInfo(UserInfo userInfo);

    Optional<RefreshToken> findByUserInfo(UserInfo userInfo);
}
