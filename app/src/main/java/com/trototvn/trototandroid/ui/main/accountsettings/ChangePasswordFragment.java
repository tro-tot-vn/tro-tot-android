package com.trototvn.trototandroid.ui.main.accountsettings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.databinding.FragmentChangePasswordBinding;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * ChangePasswordFragment - Change password screen
 * Allows user to update their password
 */
@AndroidEntryPoint
public class ChangePasswordFragment extends Fragment {

    private FragmentChangePasswordBinding binding;
    private AccountSettingsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AccountSettingsViewModel.class);

        setupUI();
        setupObservers();
    }

    private void setupUI() {
        // Back button
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Confirm button
        binding.btnConfirm.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String currentPassword = binding.etCurrentPassword.getText().toString().trim();
        String newPassword = binding.etNewPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Clear previous errors
        clearErrors();

        // Validation
        if (!validateInputs(currentPassword, newPassword, confirmPassword)) {
            return;
        }

        // Call ViewModel to change password
        viewModel.changePassword(currentPassword, newPassword);
    }

    private boolean validateInputs(String currentPassword, String newPassword, String confirmPassword) {
        boolean isValid = true;

        // Validate current password
        if (currentPassword.isEmpty()) {
            showFieldError(binding.tvCurrentPasswordError, getString(R.string.error_current_password_empty));
            isValid = false;
        }

        // Validate new password
        if (newPassword.isEmpty()) {
            showFieldError(binding.tvNewPasswordError, getString(R.string.error_new_password_empty));
            isValid = false;
        } else {
            List<String> passwordErrors = validatePasswordStrength(newPassword);
            if (!passwordErrors.isEmpty()) {
                String allErrors = String.join("\n", passwordErrors);
                showFieldError(binding.tvNewPasswordError, allErrors);
                isValid = false;
            }
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            showFieldError(binding.tvConfirmPasswordError, getString(R.string.error_confirm_password_empty));
            isValid = false;
        } else if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            showFieldError(binding.tvConfirmPasswordError, getString(R.string.error_passwords_not_match));
            isValid = false;
        }

        // Check if current password is same as new password
        if (!currentPassword.isEmpty() && !newPassword.isEmpty() && currentPassword.equals(newPassword)) {
            showFieldError(binding.tvNewPasswordError, getString(R.string.error_same_password));
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validate password strength:
     * - At least 8 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 digit
     * Returns list of all error messages (empty list if valid)
     */
    private List<String> validatePasswordStrength(String password) {
        List<String> errors = new ArrayList<>();

        if (password.length() < 8) {
            errors.add("• Mật khẩu phải có ít nhất 8 ký tự");
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.add("• Mật khẩu phải chứa ít nhất 1 chữ in hoa");
        }

        if (!password.matches(".*[a-z].*")) {
            errors.add("• Mật khẩu phải chứa ít nhất 1 chữ in thường");
        }

        if (!password.matches(".*[0-9].*")) {
            errors.add("• Mật khẩu phải chứa ít nhất 1 chữ số");
        }

        return errors;
    }

    private void showFieldError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void clearErrors() {
        binding.tvCurrentPasswordError.setVisibility(View.GONE);
        binding.tvNewPasswordError.setVisibility(View.GONE);
        binding.tvConfirmPasswordError.setVisibility(View.GONE);
    }

    private void setupObservers() {
        viewModel.getChangePasswordResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }

            if (resource.getStatus() == Resource.Status.LOADING) {
                binding.btnConfirm.setEnabled(false);
                binding.btnConfirm.setText(""); // Ẩn chữ trên nút
                binding.pbLoading.setVisibility(View.VISIBLE);
            } else {
                binding.btnConfirm.setEnabled(true);
                binding.btnConfirm.setText(R.string.confirm); // Hiện lại chữ khi xong
                binding.pbLoading.setVisibility(View.GONE);

                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    showSuccess(getString(R.string.password_changed_success));
                    // Clear fields
                    clearInputs();
                    // Navigate back after a delay
                    binding.getRoot().postDelayed(() -> {
                        NavHostFragment.findNavController(this).navigateUp();
                    }, 1500);
                } else if (resource.getStatus() == Resource.Status.ERROR) {
                    String errorMessage = resource.getMessage() != null ? resource.getMessage()
                            : getString(R.string.error_change_password);
                    showError(errorMessage);
                    Timber.e("Error changing password: %s", resource.getMessage());
                }
            }
        });
    }

    private void clearInputs() {
        binding.etCurrentPassword.setText("");
        binding.etNewPassword.setText("");
        binding.etConfirmPassword.setText("");
        clearErrors();
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

