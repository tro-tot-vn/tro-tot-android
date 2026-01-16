package com.trototvn.trototandroid.data.model.rating;

/**
 * Rating model - matches backend Rate entity
 * Used for post reviews
 */
public class Rating {
    private int rateId;
    private int numRate;  // Changed from numStar to match backend
    private String comment;
    private String createdAt;
    private Rater rater;

    // Getters and Setters
    public int getRateId() { return rateId; }
    public void setRateId(int rateId) { this.rateId = rateId; }

    public int getNumRate() { return numRate; }
    public void setNumRate(int numRate) { this.numRate = numRate; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Rater getRater() { return rater; }
    public void setRater(Rater rater) { this.rater = rater; }
}
