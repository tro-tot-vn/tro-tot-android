package com.trototvn.trototandroid.ui.auth.forgot;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.databinding.FragmentResetPasswordBinding;
import com.trototvn.trototandroid.ui.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPasswordFragment extends BaseFragment<FragmentResetPasswordBinding> {

    private ResetPasswordViewModel viewModel;
    private String resetToken;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ResetPasswordViewModel.class);
        
        if (getArguments() != null) {
            resetToken = getArguments().getString("RESET_TOKEN");
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void setupViews() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());

        binding.btnSubmit.setOnClickListener(v -> {
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
            String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";
            
            viewModel.resetPassword(resetToken, password, confirmPassword);
        });
    }

    @Override
    protected void observeData() {
        viewModel.getResetResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    showLoading(true);
                    hideError();
                    break;
                case SUCCESS:
                    showLoading(false);
                    showToast("Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
                    Navigation.findNavController(requireView()).navigate(R.id.action_resetPasswordFragment_to_loginFragment);
                    break;
                case ERROR:
                    showLoading(false);
                    showError(resource.getMessage());
                    break;
            }
        });

        viewModel.getPasswordError().observe(getViewLifecycleOwner(), error -> {
            binding.tilPassword.setError(error);
        });

        viewModel.getConfirmPasswordError().observe(getViewLifecycleOwner(), error -> {
            binding.tilConfirmPassword.setError(error);
        });
    }

    @Override
    protected void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(!isLoading);
        binding.btnSubmit.setText(isLoading ? "" : "Lưu mật khẩu mới");
    }

    private void showError(String message) {
        binding.cardError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message != null ? message : "Đã xảy ra lỗi");
    }

    private void hideError() {
        binding.cardError.setVisibility(View.GONE);
    }
}
