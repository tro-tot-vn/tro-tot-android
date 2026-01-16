package com.trototvn.trototandroid.data.model.profile;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Customer profile data model
 * Matches backend Customer entity exactly
 */
public class CustomerProfile {
    @SerializedName("customerId")
    private int customerId;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("bio")
    private String bio;

    @SerializedName("avatar")
    private String avatar;  // Google Drive URL

    @SerializedName("gender")
    private String gender;  // "Male" or "Female"

    @SerializedName("birthday")
    private Date birthday;

    @SerializedName("currentCity")
    private String currentCity;

    @SerializedName("currentDistrict")
    private String currentDistrict;

    @SerializedName("currentJob")
    private String currentJob;  // "Student" or "Employed"

    @SerializedName("joinedAt")
    private Date joinedAt;

    // Nested account data
    @SerializedName("account")
    private AccountInfo account;

    public static class AccountInfo {
        @SerializedName("email")
        private String email;

        @SerializedName("phone")
        private String phone;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    // Getters and Setters

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = currentCity;
    }

    public String getCurrentDistrict() {
        return currentDistrict;
    }

    public void setCurrentDistrict(String currentDistrict) {
        this.currentDistrict = currentDistrict;
    }

    public String getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(String currentJob) {
        this.currentJob = currentJob;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    // Helper methods

    public String getEmail() {
        return account != null ? account.getEmail() : null;
    }

    public String getPhone() {
        return account != null ? account.getPhone() : null;
    }
}
