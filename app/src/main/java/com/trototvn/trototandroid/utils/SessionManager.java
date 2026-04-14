package com.trototvn.trototandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Session Manager - Handle user session with encrypted storage
 */
public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SAVED_IDENTIFIER = "saved_identifier";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = getEncryptedSharedPreferences(context);
    }

    private SharedPreferences getEncryptedSharedPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular SharedPreferences if encrypted fails
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    /**
     * Save user session
     */
    public void saveSession(String token, String refreshToken, String userId, String userName, String userEmail) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, userName)
                .putString(KEY_USER_EMAIL, userEmail)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    /**
     * Update token only
     */
    public void updateToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    /**
     * Get access token
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * Get refresh token
     */
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Get user ID
     * Bóc tách an toàn trực tiếp từ JWT Payload để bù đắp nếu SharedPreferences thiếu
     */
    public String getUserId() {
        String token = getToken();
        if (token != null && !token.isEmpty()) {
            try {
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE), "UTF-8");
                    org.json.JSONObject json = new org.json.JSONObject(payload);
                    
                    if (json.has("customerId")) {
                        return json.optString("customerId");
                    } else if (json.has("accountId")) {
                        return json.optString("accountId");
                    } else if (json.has("userId")) {
                        return json.optString("userId");
                    }
                }
            } catch (Exception ignored) {
                // Bỏ qua lỗi và rớt xuống phương án đọc SharedPreferences (fallback)
            }
        }
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Get user name
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Clear session (logout)
     */
    public void clearSession() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }

    /**
     * Remember Me status
     */
    public void setRememberMe(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, enabled).apply();
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Saved identifier for pre-fill
     */
    public void saveIdentifier(String identifier) {
        prefs.edit().putString(KEY_SAVED_IDENTIFIER, identifier).apply();
    }

    public String getSavedIdentifier() {
        return prefs.getString(KEY_SAVED_IDENTIFIER, null);
    }

    /**
     * FCM Token management
     */
    public void saveFcmToken(String token) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
    }

    public String getFcmToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }
}
