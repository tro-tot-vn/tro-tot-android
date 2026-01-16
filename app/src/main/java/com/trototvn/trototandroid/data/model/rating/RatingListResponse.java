package com.trototvn.trototandroid.data.model.rating;

import java.util.List;

/**
 * Rating List Response with cursor pagination
 * Matches CursorPaging response from backend
 */
public class RatingListResponse {
    private List<Rating> dataPag;
    private String nextCursor;  // Date string
    private boolean hasMore;

    // Getters and Setters
    public List<Rating> getDataPag() { return dataPag; }
    public void setDataPag(List<Rating> dataPag) { this.dataPag = dataPag; }

    public String getNextCursor() { return nextCursor; }
    public void setNextCursor(String nextCursor) { this.nextCursor = nextCursor; }

    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
}
