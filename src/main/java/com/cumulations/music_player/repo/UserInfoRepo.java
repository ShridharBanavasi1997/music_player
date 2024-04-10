package com.cumulations.music_player.repo;

import com.cumulations.music_player.dto.entity.UserInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepo extends JpaRepository<UserInfo, String> ,UserInfoDao{

    Optional<UserInfo> findByEmail(String email);

    Boolean existsByEmail(String email);

}
