package com.trototvn.trototandroid.data.model.post;

/**
 * Moderation History model
 * Shows moderation actions on posts (approve/reject/hide)
 */
public class ModerationHistory {
    private int historyId;
    private int postId;
    private int adminId;
    private String actionType;  // APPROVED, REJECTED, etc.
    private String reason;      // Rejection/moderation reason
    private String execAt;      // Execution timestamp

    // Getters and Setters
    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getExecAt() { return execAt; }
    public void setExecAt(String execAt) { this.execAt = execAt; }
}
