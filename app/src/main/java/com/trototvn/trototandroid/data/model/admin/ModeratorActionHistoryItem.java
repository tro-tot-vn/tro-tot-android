package com.trototvn.trototandroid.data.model.admin;

/**
 * One record from GET api/admin/moderators/{moderatorId}/history
 */
public class ModeratorActionHistoryItem {
    private int postId;
    private String actionType;
    private String reason;
    private String execAt;
    private Post post;

    public int getPostId() {
        return postId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getReason() {
        return reason;
    }

    public String getExecAt() {
        return execAt;
    }

    public Post getPost() {
        return post;
    }

    public String getPostTitle() {
        return post != null ? post.getTitle() : null;
    }

    public static class Post {
        private int postId;
        private String title;

        public int getPostId() {
            return postId;
        }

        public String getTitle() {
            return title;
        }
    }
}
