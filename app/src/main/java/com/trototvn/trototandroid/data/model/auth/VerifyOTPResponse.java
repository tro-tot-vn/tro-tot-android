package com.trototvn.trototandroid.data.model.auth;

import com.google.gson.annotations.SerializedName;

public class VerifyOTPResponse {
    @SerializedName("resetToken")
    private String resetToken;

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
}
