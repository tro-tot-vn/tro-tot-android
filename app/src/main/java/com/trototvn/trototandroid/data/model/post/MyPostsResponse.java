package com.trototvn.trototandroid.data.model.post;

import java.util.List;

/**
 * My Posts Response with cursor pagination
 * Matches backend GetPostByStatusResponse
 */
public class MyPostsResponse {
    private List<MyPost> dataPag;
    private Integer nextCursor;  // postId for cursor
    private boolean hasMore;

    // Getters and Setters
    public List<MyPost> getDataPag() { return dataPag; }
    public void setDataPag(List<MyPost> dataPag) { this.dataPag = dataPag; }

    public Integer getNextCursor() { return nextCursor; }
    public void setNextCursor(Integer nextCursor) { this.nextCursor = nextCursor; }

    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
}
