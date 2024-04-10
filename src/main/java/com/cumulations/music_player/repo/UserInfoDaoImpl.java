package com.cumulations.music_player.repo;

import com.cumulations.music_player.dto.entity.UserInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

public class UserInfoDaoImpl implements UserInfoDao{
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    @Transactional
    public void saveUserInfo(UserInfo userInfo) {
        entityManager.persist(userInfo);
    }
}
