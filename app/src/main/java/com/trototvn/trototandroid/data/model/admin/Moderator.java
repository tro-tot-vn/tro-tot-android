package com.trototvn.trototandroid.data.model.admin;

/**
 * Moderator summary / profile.
 * Shape shared by GET api/admin/moderators and GET api/admin/moderators/{id}/profile.
 */
public class Moderator {

    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_INACTIVE = "Inactive";
    public static final String STATUS_BLOCKED = "Blocked";

    private int adminId;
    private String firstName;
    private String lastName;
    private String birthday;
    private String gender;
    private String joinedAt;
    private Account account;

    public int getAdminId() {
        return adminId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getGender() {
        return gender;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public Account getAccount() {
        return account;
    }

    public String getFullName() {
        String f = firstName != null ? firstName : "";
        String l = lastName != null ? lastName : "";
        return (f + " " + l).trim();
    }

    public String getStatus() {
        return account != null ? account.getStatus() : null;
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(getStatus());
    }

    public static class Account {
        private String email;
        private String phone;
        private String status;

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getStatus() {
            return status;
        }
    }
}
