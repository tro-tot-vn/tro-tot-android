package com.trototvn.trototandroid.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

/**
 * View utility class for UI operations
 */
public class ViewUtils {

    /**
     * Show toast message
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show long toast message
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Show snackbar
     */
    public static void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Show snackbar with action
     */
    public static void showSnackbarWithAction(View view, String message, String actionText,
            View.OnClickListener listener) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText, listener)
                .show();
    }

    /**
     * Show view
     */
    public static void show(View view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide view
     */
    public static void hide(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * Make view invisible (still takes space)
     */
    public static void invisible(View view) {
        if (view != null) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Toggle view visibility
     */
    public static void toggle(View view) {
        if (view != null) {
            view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Hide keyboard
     */
    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Show keyboard
     */
    public static void showKeyboard(Activity activity, View view) {
        if (activity != null && view != null) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * Enable view
     */
    public static void enable(View view) {
        if (view != null) {
            view.setEnabled(true);
        }
    }

    /**
     * Disable view
     */
    public static void disable(View view) {
        if (view != null) {
            view.setEnabled(false);
        }
    }
}
