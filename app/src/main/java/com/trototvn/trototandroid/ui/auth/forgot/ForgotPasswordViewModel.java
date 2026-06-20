package com.trototvn.trototandroid.ui.auth.forgot;

import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.auth.OTPResponse;
import com.trototvn.trototandroid.data.repository.AuthRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;
import com.trototvn.trototandroid.utils.StringUtils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

@HiltViewModel
public class ForgotPasswordViewModel extends BaseViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<OTPResponse>> forgotPasswordResult = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();

    @Inject
    public ForgotPasswordViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public MutableLiveData<Resource<OTPResponse>> getForgotPasswordResult() {
        return forgotPasswordResult;
    }

    public MutableLiveData<String> getEmailError() {
        return emailError;
    }

    public void sendOtp(String email) {
        emailError.setValue(null);

        if (email == null || email.trim().isEmpty()) {
            emailError.setValue("Vui lòng nhập email");
            return;
        } else if (!StringUtils.isValidEmail(email)) {
            emailError.setValue("Định dạng email không hợp lệ");
            return;
        }

        handleLoading(forgotPasswordResult);

        addDisposable(
                authRepository.forgotPassword(email)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resource -> forgotPasswordResult.setValue(resource),
                                throwable -> handleError(forgotPasswordResult, throwable.getMessage())
                        )
        );
    }
}
