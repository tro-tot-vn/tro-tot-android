package com.trototvn.trototandroid.data.model.post;

/**
 * Owner Account model - matches backend Account interface
 */
public class OwnerAccount {
    private int accountId;
    private String email;
    private String phone;

    // Getters and Setters
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
