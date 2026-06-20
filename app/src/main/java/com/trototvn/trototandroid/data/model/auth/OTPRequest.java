package com.trototvn.trototandroid.data.model.auth;

public class OTPRequest {
    private String email;

    public OTPRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
