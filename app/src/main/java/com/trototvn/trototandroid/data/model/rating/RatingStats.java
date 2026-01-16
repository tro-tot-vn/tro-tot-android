package com.trototvn.trototandroid.data.model.rating;

/**
 * Rating Statistics model
 * Matches backend avgRate response
 */
public class RatingStats {
    private double avgRate;
    private int countRate;

    public RatingStats() {}

    public RatingStats(double avgRate, int countRate) {
        this.avgRate = avgRate;
        this.countRate = countRate;
    }

    public double getAvgRate() { return avgRate; }
    public void setAvgRate(double avgRate) { this.avgRate = avgRate; }

    public int getCountRate() { return countRate; }
    public void setCountRate(int countRate) { this.countRate = countRate; }
}
