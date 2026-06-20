package com.trototvn.trototandroid.ui.auth.forgot;

import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.auth.VerifyOTPResponse;
import com.trototvn.trototandroid.data.repository.AuthRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

@HiltViewModel
public class VerifyOtpViewModel extends BaseViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<String>> verifyResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> resendResult = new MutableLiveData<>();
    private final MutableLiveData<String> otpError = new MutableLiveData<>();

    @Inject
    public VerifyOtpViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public MutableLiveData<Resource<String>> getVerifyResult() {
        return verifyResult;
    }

    public MutableLiveData<Resource<String>> getResendResult() {
        return resendResult;
    }

    public MutableLiveData<String> getOtpError() {
        return otpError;
    }

    public void verifyOtp(String email, String otp, String otpType) {
        otpError.setValue(null);

        if (otp == null || otp.trim().isEmpty()) {
            otpError.setValue("Vui lòng nhập mã OTP");
            return;
        } else if (otp.trim().length() != 6) {
            otpError.setValue("Mã OTP phải có 6 chữ số");
            return;
        }

        handleLoading(verifyResult);

        if ("REGISTER".equals(otpType)) {
            addDisposable(
                    authRepository.verifyOtpRegister(email, otp)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    resource -> {
                                        if (resource.getStatus() == Resource.Status.SUCCESS) {
                                            verifyResult.setValue(Resource.success("SUCCESS"));
                                        } else if (resource.getStatus() == Resource.Status.ERROR) {
                                            verifyResult.setValue(Resource.error(resource.getMessage(), null));
                                        }
                                    },
                                    throwable -> handleError(verifyResult, throwable.getMessage())
                            )
            );
        } else if ("FORGOT_PASSWORD".equals(otpType)) {
            addDisposable(
                    authRepository.verifyOtp(email, otp)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    resource -> {
                                        if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                                            verifyResult.setValue(Resource.success(resource.getData().getResetToken()));
                                        } else if (resource.getStatus() == Resource.Status.ERROR) {
                                            verifyResult.setValue(Resource.error(resource.getMessage(), null));
                                        }
                                    },
                                    throwable -> handleError(verifyResult, throwable.getMessage())
                            )
            );
        }
    }

    public void resendOtp(String email, String otpType) {
        handleLoading(resendResult);

        if ("REGISTER".equals(otpType)) {
            addDisposable(
                    authRepository.sendOtpRegister(email)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    resource -> {
                                        if (resource.getStatus() == Resource.Status.SUCCESS) {
                                            resendResult.setValue(Resource.success("SUCCESS"));
                                        } else if (resource.getStatus() == Resource.Status.ERROR) {
                                            resendResult.setValue(Resource.error(resource.getMessage(), null));
                                        }
                                    },
                                    throwable -> handleError(resendResult, throwable.getMessage())
                            )
            );
        } else if ("FORGOT_PASSWORD".equals(otpType)) {
            addDisposable(
                    authRepository.forgotPassword(email)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    resource -> {
                                        if (resource.getStatus() == Resource.Status.SUCCESS) {
                                            resendResult.setValue(Resource.success("SUCCESS"));
                                        } else if (resource.getStatus() == Resource.Status.ERROR) {
                                            resendResult.setValue(Resource.error(resource.getMessage(), null));
                                        }
                                    },
                                    throwable -> handleError(resendResult, throwable.getMessage())
                            )
            );
        }
    }
}
