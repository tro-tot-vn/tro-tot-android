package com.trototvn.trototandroid.data.model.post;

/**
 * Request for hiding/unhiding post
 */
public class HidePostRequest {
    private int postId;

    public HidePostRequest(int postId) {
        this.postId = postId;
    }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
}
