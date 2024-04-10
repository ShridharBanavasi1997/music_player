package com.cumulations.music_player.dto.response_body;

public class LoginResponse {
    private String email;

    private String accessToken;

    private String refreshToken;

    private long issuedAt;

    private long expiredAt;

    public LoginResponse(String email, String accessToken, String refreshToken, long issuedAt, long expiredAt) {
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public long getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(long expiredAt) {
        this.expiredAt = expiredAt;
    }
}
