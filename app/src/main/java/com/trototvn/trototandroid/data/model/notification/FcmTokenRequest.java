package com.trototvn.trototandroid.data.model.notification;

public class FcmTokenRequest {
    private String fcmToken;
    private String platform;

    public FcmTokenRequest(String fcmToken) {
        this.fcmToken = fcmToken;
        this.platform = "android";
    }

    public FcmTokenRequest(String fcmToken, String platform) {
        this.fcmToken = fcmToken;
        this.platform = platform;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
