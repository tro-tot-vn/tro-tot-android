package com.trototvn.trototandroid.ui.auth.forgot;

import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.repository.AuthRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

@HiltViewModel
public class ResetPasswordViewModel extends BaseViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<Void>> resetResult = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPasswordError = new MutableLiveData<>();

    @Inject
    public ResetPasswordViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public MutableLiveData<Resource<Void>> getResetResult() {
        return resetResult;
    }

    public MutableLiveData<String> getPasswordError() {
        return passwordError;
    }

    public MutableLiveData<String> getConfirmPasswordError() {
        return confirmPasswordError;
    }

    public void resetPassword(String resetToken, String password, String confirmPassword) {
        passwordError.setValue(null);
        confirmPasswordError.setValue(null);

        boolean isValid = true;

        if (password == null || password.trim().isEmpty()) {
            passwordError.setValue("Vui lòng nhập mật khẩu mới");
            isValid = false;
        } else if (password.length() < 6) {
            passwordError.setValue("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            confirmPasswordError.setValue("Vui lòng xác nhận mật khẩu");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordError.setValue("Mật khẩu xác nhận không khớp");
            isValid = false;
        }

        if (!isValid) return;

        handleLoading(resetResult);

        addDisposable(
                authRepository.resetPassword(resetToken, password)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resource -> resetResult.setValue(resource),
                                throwable -> handleError(resetResult, throwable.getMessage())
                        )
        );
    }
}
