package com.trototvn.trototandroid.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.Window;

import com.trototvn.trototandroid.databinding.DialogLoadingBinding;

/**
 * Custom loading dialog
 */
public class LoadingDialog {

    private final Dialog dialog;
    private final DialogLoadingBinding binding;

    public LoadingDialog(Context context) {
        binding = DialogLoadingBinding.inflate(LayoutInflater.from(context));

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /**
     * Show loading dialog
     */
    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * Show with custom message
     */
    public void show(String message) {
        binding.tvMessage.setText(message);
        show();
    }

    /**
     * Dismiss loading dialog
     */
    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * Check if showing
     */
    public boolean isShowing() {
        return dialog.isShowing();
    }
}
