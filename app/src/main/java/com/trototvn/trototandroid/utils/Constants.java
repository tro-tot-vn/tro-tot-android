package com.trototvn.trototandroid.utils;

/**
 * Constants class for app-wide constants
 */
public class Constants {

    // API related
    public static final String BASE_URL = "https://your-api-url.com/";
    public static final int TIMEOUT_SECONDS = 30;

    // Pagination
    public static final int PAGE_SIZE = 20;
    public static final int INITIAL_PAGE = 1;

    // Intent extras
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DATA = "data";

    // Request codes
    public static final int REQUEST_CODE_PICK_IMAGE = 1001;
    public static final int REQUEST_CODE_CAMERA = 1002;
    public static final int REQUEST_CODE_PERMISSION = 1003;

    // Preferences keys (if needed beyond SessionManager)
    public static final String PREF_THEME = "theme";
    public static final String PREF_LANGUAGE = "language";
    public static final String PREF_NOTIFICATION_ENABLED = "notification_enabled";

    // Image upload
    public static final int MAX_IMAGE_SIZE_MB = 5;
    public static final int IMAGE_QUALITY = 80;

    // Validation
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 50;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;

    // Date formats (also in DateUtils, but useful to have here)
    public static final String FORMAT_DATE = "dd/MM/yyyy";
    public static final String FORMAT_DATETIME = "dd/MM/yyyy HH:mm";
    public static final String FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}
