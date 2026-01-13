package com.trototvn.trototandroid.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.Window;

import com.trototvn.trototandroid.databinding.DialogConfirmBinding;

/**
 * Custom confirm dialog
 */
public class ConfirmDialog {

    public interface OnConfirmListener {
        void onConfirm();

        void onCancel();
    }

    private final Dialog dialog;
    private final DialogConfirmBinding binding;

    public ConfirmDialog(Context context, String title, String message, OnConfirmListener listener) {
        binding = DialogConfirmBinding.inflate(LayoutInflater.from(context));

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Set content
        binding.tvTitle.setText(title);
        binding.tvMessage.setText(message);

        // Set listeners
        binding.btnConfirm.setOnClickListener(v -> {
            listener.onConfirm();
            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> {
            listener.onCancel();
            dismiss();
        });
    }

    /**
     * Show dialog
     */
    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * Dismiss dialog
     */
    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * Builder for custom button texts
     */
    public static class Builder {
        private final Context context;
        private String title = "Confirm";
        private String message = "Are you sure?";
        private String positiveText = "OK";
        private String negativeText = "Cancel";
        private OnConfirmListener listener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setPositiveButton(String text) {
            this.positiveText = text;
            return this;
        }

        public Builder setNegativeButton(String text) {
            this.negativeText = text;
            return this;
        }

        public Builder setListener(OnConfirmListener listener) {
            this.listener = listener;
            return this;
        }

        public ConfirmDialog build() {
            ConfirmDialog dialog = new ConfirmDialog(context, title, message, listener);
            dialog.binding.btnConfirm.setText(positiveText);
            dialog.binding.btnCancel.setText(negativeText);
            return dialog;
        }

        public void show() {
            build().show();
        }
    }
}
