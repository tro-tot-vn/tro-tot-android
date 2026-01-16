package com.trototvn.trototandroid.data.model.rating;

/**
 * Request body for adding rating
 */
public class AddRatingRequest {
    private int numStar;  // Changed from numRate to match backend
    private String comment;

    public AddRatingRequest(int numStar, String comment) {
        this.numStar = numStar;
        this.comment = comment;
    }

    public int getNumStar() { return numStar; }
    public void setNumStar(int numStar) { this.numStar = numStar; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
