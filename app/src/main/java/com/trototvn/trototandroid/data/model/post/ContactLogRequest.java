package com.trototvn.trototandroid.data.model.post;

/**
 * Request body for contact logging
 */
public class ContactLogRequest {
    private int postId;

    public ContactLogRequest(int postId) {
        this.postId = postId;
    }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
}
