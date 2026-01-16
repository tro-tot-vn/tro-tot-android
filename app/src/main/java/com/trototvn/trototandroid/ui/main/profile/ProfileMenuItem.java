package com.trototvn.trototandroid.ui.main.profile;

/**
 * Menu item model for profile screen
 */
public class ProfileMenuItem {
    
    public enum ItemType {
        EDIT_PROFILE,
        SAVED_POSTS,
        VIEW_HISTORY,
        SUBSCRIPTIONS,
        SETTINGS,
        LOGOUT
    }
    
    private final int iconRes;
    private final String title;
    private final String subtitle;  // For count display
    private final ItemType type;

    public ProfileMenuItem(int iconRes, String title, String subtitle, ItemType type) {
        this.iconRes = iconRes;
        this.title = title;
        this.subtitle = subtitle;
        this.type = type;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public ItemType getType() {
        return type;
    }
}
