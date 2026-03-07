package com.trototvn.trototandroid.ui.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.databinding.FragmentLoginBinding;
import com.trototvn.trototandroid.ui.auth.AuthActivity;
import com.trototvn.trototandroid.ui.base.BaseFragment;
import com.trototvn.trototandroid.ui.main.MainActivity;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * LoginFragment - Clean implementation with MVVM pattern
 */
@AndroidEntryPoint
public class LoginFragment extends BaseFragment<FragmentLoginBinding> {

    private LoginViewModel viewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void setupViews() {
        // Login button click
        binding.btnLogin.setOnClickListener(v -> handleLogin());

        // Register link click
        binding.tvRegisterLink.setOnClickListener(v -> navigateToRegister());

        // Forgot password link click
        binding.tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());

        // Handle Remember Me checkbox
        binding.cbRememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setRememberMe(isChecked);
        });

        // Pre-fill identifier if saved
        String savedIdentifier = viewModel.getSavedIdentifier();
        if (savedIdentifier != null) {
            binding.etIdentifier.setText(savedIdentifier);
            binding.cbRememberMe.setChecked(true);
        }
    }

    @Override
    protected void observeData() {
        viewModel.getLoginResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null)
                return;

            switch (resource.getStatus()) {
                case LOADING:
                    showLoading(true);
                    binding.cardError.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    showLoading(false);
                    // Navigate to Main Activity
                    if (getActivity() instanceof AuthActivity) {
                        ((AuthActivity) getActivity()).navigateToMainApp();
                    }
                    break;
                case ERROR:
                    showLoading(false);
                    binding.cardError.setVisibility(View.VISIBLE);
                    binding.tvErrorMessage.setText(resource.getMessage());
                    break;
            }
        });

        // Observe validation errors
        viewModel.getIdentifierError().observe(getViewLifecycleOwner(), error -> {
            binding.tilIdentifier.setError(error);
        });

        viewModel.getPasswordError().observe(getViewLifecycleOwner(), error -> {
            binding.tilPassword.setError(error);
        });

        viewModel.getRememberMe().observe(getViewLifecycleOwner(), isChecked -> {
            if (binding.cbRememberMe.isChecked() != isChecked) {
                binding.cbRememberMe.setChecked(isChecked);
            }
        });
    }

    /**
     * Handle login button click
     */
    private void handleLogin() {
        String identifier = getTextOrEmpty(binding.etIdentifier);
        String password = getTextOrEmpty(binding.etPassword);

        viewModel.login(identifier, password);
    }

    /**
     * Handle login result from ViewModel
     */
    private void handleLoginResult(Resource<?> resource) {
        switch (resource.getStatus()) {
            case LOADING:
                showLoading(true);
                hideError();
                break;

            case SUCCESS:
                showLoading(false);
                showToast("Đăng nhập thành công!");
                navigateToMainApp();
                break;

            case ERROR:
                showLoading(false);
                showError(resource.getMessage());
                break;
        }
    }

    /**
     * Navigate to main app after successful login
     */
    private void navigateToMainApp() {
        if (getActivity() instanceof AuthActivity) {
            ((AuthActivity) getActivity()).navigateToMainApp();
        }
    }

    /**
     * Navigate to register screen
     */
    private void navigateToRegister() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_loginFragment_to_registerFragment);
    }

    /**
     * Navigate to forgot password screen
     */
    private void navigateToForgotPassword() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
    }

    /**
     * Show loading state
     */
    @Override
    protected void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
    }

    /**
     * Show error banner
     */
    private void showError(String message) {
        binding.cardError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message != null ? message : "Đã xảy ra lỗi");
    }

    /**
     * Hide error banner
     */
    private void hideError() {
        binding.cardError.setVisibility(View.GONE);
    }

    /**
     * Helper to get text from EditText or empty string
     */
    private String getTextOrEmpty(android.widget.EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
