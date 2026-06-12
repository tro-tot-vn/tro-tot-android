package com.trototvn.trototandroid.data.model.admin;

/**
 * Body for POST api/admin/posts/{postId}/moderate
 */
public class ModeratePostRequest {

    public static final String ACTION_APPROVED = "Approved";
    public static final String ACTION_REJECTED = "Rejected";

    private final String actionType; // "Approved" | "Rejected"
    private final String reason;      // required when Rejected
    private final Boolean isHateContent;

    public ModeratePostRequest(String actionType, String reason, Boolean isHateContent) {
        this.actionType = actionType;
        this.reason = reason;
        this.isHateContent = isHateContent;
    }

    public static ModeratePostRequest approve(String reason) {
        return new ModeratePostRequest(ACTION_APPROVED, reason, false);
    }

    public static ModeratePostRequest reject(String reason, boolean isHateContent) {
        return new ModeratePostRequest(ACTION_REJECTED, reason, isHateContent);
    }

    public String getActionType() {
        return actionType;
    }

    public String getReason() {
        return reason;
    }

    public Boolean getIsHateContent() {
        return isHateContent;
    }
}
