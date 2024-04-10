package com.cumulations.music_player.repo;

import com.cumulations.music_player.dto.entity.UserInfo;

public interface UserInfoDao {
    void saveUserInfo(UserInfo userInfo);
}
