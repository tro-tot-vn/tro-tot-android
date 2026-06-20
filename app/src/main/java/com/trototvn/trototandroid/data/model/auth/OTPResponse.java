package com.trototvn.trototandroid.data.model.auth;

public class OTPResponse {
    private String message;
    private int ttl;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }
}
