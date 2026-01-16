package com.trototvn.trototandroid.data.model.post;

/**
 * Post Owner model - matches backend Owner interface
 */
public class PostOwner {
    private int customerId;
    private String firstName;
    private String lastName;
    private String avatar;
    private String currentCity;
    private String currentDistrict;
    private String currentJob;
    private OwnerAccount account;
    private String joinedAt;

    // Getters and Setters
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getCurrentCity() { return currentCity; }
    public void setCurrentCity(String currentCity) { this.currentCity = currentCity; }

    public String getCurrentDistrict() { return currentDistrict; }
    public void setCurrentDistrict(String currentDistrict) {
        this.currentDistrict = currentDistrict;
    }

    public String getCurrentJob() { return currentJob; }
    public void setCurrentJob(String currentJob) { this.currentJob = currentJob; }

    public OwnerAccount getAccount() { return account; }
    public void setAccount(OwnerAccount account) { this.account = account; }

    public String getJoinedAt() { return joinedAt; }
    public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }

    /**
     * Helper: Get full name
     */
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
