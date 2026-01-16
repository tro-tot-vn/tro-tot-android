package com.trototvn.trototandroid.data.model.rating;

/**
 * Rater model - customer who rated the post
 */
public class Rater {
    private int customerId;
    private String firstName;
    private String lastName;
    private String avatar;

    // Getters and Setters
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    /**
     * Helper: Get full name
     */
    public String getFullName() {
        return (lastName != null ? lastName : "") + " " + (firstName != null ? firstName : "");
    }
}
