package com.trototvn.trototandroid.data.model.profile;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Subscription model - Location alert subscription
 * Matches backend SubscriptionAreaPost entity
 */
public class Subscription {
    @SerializedName("subscriptionId")
    private int subscriptionId;

    @SerializedName("customerId")
    private int customerId;

    @SerializedName("city")
    private String city;

    @SerializedName("district")
    private String district;

    @SerializedName("createdAt")
    private Date createdAt;

    // Getters and Setters

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get display text for subscription
     * @return "District, City"
     */
    public String getDisplayText() {
        return district + ", " + city;
    }
}
