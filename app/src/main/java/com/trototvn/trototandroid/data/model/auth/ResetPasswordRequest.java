package com.trototvn.trototandroid.data.model.auth;

public class ResetPasswordRequest {
    private String resetToken;
    private String password;

    public ResetPasswordRequest(String resetToken, String password) {
        this.resetToken = resetToken;
        this.password = password;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
