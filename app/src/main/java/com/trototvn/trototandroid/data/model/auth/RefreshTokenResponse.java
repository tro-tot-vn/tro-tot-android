package com.trototvn.trototandroid.data.model.auth;

/**
 * RefreshTokenResponse - Backend ONLY returns accessToken, not refreshToken
 */
public class RefreshTokenResponse {
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
