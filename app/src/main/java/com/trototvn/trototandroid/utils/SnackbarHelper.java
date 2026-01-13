package com.trototvn.trototandroid.utils;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/**
 * Helper class for Snackbar operations
 */
public class SnackbarHelper {

    /**
     * Show simple snackbar
     */
    public static void showShort(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Show long snackbar
     */
    public static void showLong(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Show snackbar with action
     */
    public static void showWithAction(View view, String message, String actionText, View.OnClickListener listener) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText, listener)
                .show();
    }

    /**
     * Show error snackbar (red background)
     */
    public static void showError(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(0xFFD32F2F); // Red color
        snackbar.setTextColor(0xFFFFFFFF); // White text
        snackbar.show();
    }

    /**
     * Show success snackbar (green background)
     */
    public static void showSuccess(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(0xFF388E3C); // Green color
        snackbar.setTextColor(0xFFFFFFFF); // White text
        snackbar.show();
    }

    /**
     * Show warning snackbar (orange background)
     */
    public static void showWarning(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(0xFFF57C00); // Orange color
        snackbar.setTextColor(0xFFFFFFFF); // White text
        snackbar.show();
    }

    /**
     * Show info snackbar (blue background)
     */
    public static void showInfo(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(0xFF1976D2); // Blue color
        snackbar.setTextColor(0xFFFFFFFF); // White text
        snackbar.show();
    }
}
