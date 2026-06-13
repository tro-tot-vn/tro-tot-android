package com.trototvn.trototandroid.data.model.admin;

/**
 * One record from GET api/admin/posts/{postId}/history
 */
public class PostModerationHistoryItem {
    private int postId;
    private String actionType;
    private String reason;
    private String execAt;
    private Admin admin;

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

    public Admin getAdmin() {
        return admin;
    }

    public static class Admin {
        private int accountId;
        private String firstName;
        private String lastName;
        private Account account;

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public Account getAccount() {
            return account;
        }

        public String getFullName() {
            String f = firstName != null ? firstName : "";
            String l = lastName != null ? lastName : "";
            return (l + " " + f).trim();
        }
    }

    public static class Account {
        private String email;

        public String getEmail() {
            return email;
        }
    }
}
