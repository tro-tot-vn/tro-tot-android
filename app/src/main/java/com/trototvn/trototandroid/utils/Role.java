package com.trototvn.trototandroid.utils;

/**
 * Account role names as seeded by the backend (Role.roleName).
 * NOTE: the backend admin controller currently authorizes moderation on the scope
 * ["Admin","Manager"], but no "Admin" role is seeded — moderators carry roleName "Moderator".
 * We gate the Android admin area on the real seeded roles {Moderator, Manager}.
 */
public final class Role {

    public static final String MANAGER = "Manager";
    public static final String MODERATOR = "Moderator";
    public static final String CUSTOMER = "Customer";

    private Role() {
    }

    /** Roles allowed into the admin area (dashboard + post review). */
    public static boolean isAdminArea(String role) {
        return MANAGER.equals(role) || MODERATOR.equals(role);
    }

    /** Manager-only capabilities (moderator management). */
    public static boolean isManager(String role) {
        return MANAGER.equals(role);
    }
}
