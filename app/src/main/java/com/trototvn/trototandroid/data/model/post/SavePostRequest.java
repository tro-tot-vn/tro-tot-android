package com.trototvn.trototandroid.data.model.post;

/**
 * Request body for saving post
 */
public class SavePostRequest {
    private int postId;

    public SavePostRequest(int postId) {
        this.postId = postId;
    }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
}
