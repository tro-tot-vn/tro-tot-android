package com.trototvn.trototandroid.ui.auth.forgot;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.databinding.FragmentVerifyOtpBinding;
import com.trototvn.trototandroid.ui.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VerifyOtpFragment extends BaseFragment<FragmentVerifyOtpBinding> {

    private VerifyOtpViewModel viewModel;
    private String email;
    private String otpType;
    private CountDownTimer countDownTimer;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(VerifyOtpViewModel.class);
        
        if (getArguments() != null) {
            email = getArguments().getString("EMAIL");
            otpType = getArguments().getString("OTP_TYPE");
        }

        super.onViewCreated(view, savedInstanceState);
        
        binding.tvDescription.setText("Vui lòng nhập mã OTP đã được gửi đến email " + email);
        startResendTimer();
    }

    @Override
    protected void setupViews() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());

        binding.btnSubmit.setOnClickListener(v -> {
            String otp = binding.etOtp.getText() != null ? binding.etOtp.getText().toString().trim() : "";
            viewModel.verifyOtp(email, otp, otpType);
        });

        binding.tvResend.setOnClickListener(v -> {
            viewModel.resendOtp(email, otpType);
        });
    }

    @Override
    protected void observeData() {
        viewModel.getVerifyResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    showLoading(true);
                    hideError();
                    break;
                case SUCCESS:
                    showLoading(false);
                    if ("REGISTER".equals(otpType)) {
                        showToast("Đăng ký hoàn tất! Vui lòng đăng nhập.");
                        Navigation.findNavController(requireView()).navigate(R.id.action_verifyOtpFragment_to_loginFragment);
                    } else if ("FORGOT_PASSWORD".equals(otpType)) {
                        String resetToken = resource.getData();
                        Bundle bundle = new Bundle();
                        bundle.putString("RESET_TOKEN", resetToken);
                        Navigation.findNavController(requireView()).navigate(R.id.action_verifyOtpFragment_to_resetPasswordFragment, bundle);
                    }
                    break;
                case ERROR:
                    showLoading(false);
                    showError(resource.getMessage());
                    break;
            }
        });

        viewModel.getResendResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    // Optional: show a small loader for resend
                    break;
                case SUCCESS:
                    showToast("Mã OTP mới đã được gửi");
                    startResendTimer();
                    break;
                case ERROR:
                    showError(resource.getMessage());
                    break;
            }
        });

        viewModel.getOtpError().observe(getViewLifecycleOwner(), error -> {
            binding.tilOtp.setError(error);
        });
    }

    private void startResendTimer() {
        binding.tvResend.setEnabled(false);
        binding.tvResend.setTextColor(getResources().getColor(R.color.text_secondary, null));

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvResend.setText("Gửi lại (" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                binding.tvResend.setEnabled(true);
                binding.tvResend.setText("Gửi lại");
                binding.tvResend.setTextColor(getResources().getColor(R.color.primary, null));
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(!isLoading);
        binding.btnSubmit.setText(isLoading ? "" : "Xác nhận");
    }

    private void showError(String message) {
        binding.cardError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message != null ? message : "Đã xảy ra lỗi");
    }

    private void hideError() {
        binding.cardError.setVisibility(View.GONE);
    }
}
