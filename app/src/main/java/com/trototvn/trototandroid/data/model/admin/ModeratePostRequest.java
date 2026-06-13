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
        // Approval reason is optional. Send null (Gson omits it) when blank, otherwise the
        // backend DTO's @minLength(1) on `reason` rejects an empty string with HTTP 400.
        return new ModeratePostRequest(ACTION_APPROVED, blankToNull(reason), false);
    }

    public static ModeratePostRequest reject(String reason, boolean isHateContent) {
        return new ModeratePostRequest(ACTION_REJECTED, blankToNull(reason), isHateContent);
    }

    private static String blankToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
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
