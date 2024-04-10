package com.cumulations.music_player.service;

import com.cumulations.music_player.dto.entity.RefreshToken;
import com.cumulations.music_player.dto.entity.UserInfo;
import com.cumulations.music_player.dto.exception.TokenRefreshException;
import com.cumulations.music_player.repo.RefreshTokenRepo;
import com.cumulations.music_player.repo.UserInfoRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${cumulations.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Autowired
    private UserInfoRepo userInfoRepo;

    public boolean refreshTokenExistsForUser(String email){
        return refreshTokenRepo.existsByUserInfo(userInfoRepo.findByEmail(email).get());
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    public RefreshToken createRefreshToken(String emailId) {
        RefreshToken refreshToken = new RefreshToken();

        if(refreshTokenExistsForUser(emailId)){
            RefreshToken refreshToken1 = refreshTokenRepo.findByUserInfo(userInfoRepo.findByEmail(emailId).get()).get();
            refreshTokenRepo.delete(refreshToken1);
        }

        refreshToken.setUserInfo(userInfoRepo.findByEmail(emailId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepo.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepo.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public int deleteByEmail(String email) {
        UserInfo userInfo = userInfoRepo.findByEmail(email).get();
        return refreshTokenRepo.deleteByUserInfo(userInfo);
    }
}
