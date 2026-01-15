package com.trototvn.trototandroid.data.model.auth;

/**
 * Login response model
 */
public class LoginResponse {
    private Account account;
    private Token token;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
