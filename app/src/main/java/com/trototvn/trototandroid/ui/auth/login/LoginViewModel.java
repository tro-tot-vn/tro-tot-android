package com.trototvn.trototandroid.ui.auth.login;

import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.auth.LoginResponse;
import com.trototvn.trototandroid.data.repository.AuthRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;
import com.trototvn.trototandroid.utils.SessionManager;
import com.trototvn.trototandroid.utils.StringUtils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * LoginViewModel - Handles login logic and validation
 */
@HiltViewModel
public class LoginViewModel extends BaseViewModel {

    private final AuthRepository authRepository;
    private final SessionManager sessionManager;

    // LiveData for login result
    private final MutableLiveData<Resource<LoginResponse>> loginResult = new MutableLiveData<>();

    // LiveData for validation errors
    private final MutableLiveData<String> identifierError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();

    // LiveData for Remember Me state
    private final MutableLiveData<Boolean> rememberMe = new MutableLiveData<>(false);

    @Inject
    public LoginViewModel(AuthRepository authRepository, SessionManager sessionManager) {
        this.authRepository = authRepository;
        this.sessionManager = sessionManager;
        this.rememberMe.setValue(sessionManager.isRememberMe());
    }

    public MutableLiveData<Resource<LoginResponse>> getLoginResult() {
        return loginResult;
    }

    public MutableLiveData<String> getIdentifierError() {
        return identifierError;
    }

    public MutableLiveData<String> getPasswordError() {
        return passwordError;
    }

    public MutableLiveData<Boolean> getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean enabled) {
        rememberMe.setValue(enabled);
        sessionManager.setRememberMe(enabled);
    }

    public String getSavedIdentifier() {
        return sessionManager.getSavedIdentifier();
    }

    /**
     * Perform login
     */
    public void login(String identifier, String password) {
        // Clear previous errors
        identifierError.setValue(null);
        passwordError.setValue(null);

        // Validate input
        if (!validateInput(identifier, password)) {
            return;
        }

        // Show loading
        Timber.d("Login attempt for identifier: %s", identifier);
        handleLoading(loginResult);

        // Call repository
        addDisposable(
                authRepository.login(identifier, password)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resource -> {
                                    if (resource.getStatus() == Resource.Status.SUCCESS) {
                                        handleSuccessfulLogin(identifier);
                                    }
                                    loginResult.setValue(resource);
                                },
                                throwable -> handleError(loginResult, throwable.getMessage())));
    }

    private void handleSuccessfulLogin(String identifier) {
        if (Boolean.TRUE.equals(rememberMe.getValue())) {
            sessionManager.saveIdentifier(identifier);
        } else {
            sessionManager.saveIdentifier(null);
        }
    }

    /**
     * Validate login input
     */
    private boolean validateInput(String identifier, String password) {
        boolean isValid = true;

        if (identifier == null || identifier.trim().isEmpty()) {
            identifierError.setValue("Vui lòng nhập số điện thoại hoặc email");
            isValid = false;
        } else if (!StringUtils.isValidEmail(identifier) && !StringUtils.isValidPhone(identifier)) {
            identifierError.setValue("Định dạng email hoặc số điện thoại không hợp lệ");
            isValid = false;
        }

        if (password == null || password.trim().isEmpty()) {
            passwordError.setValue("Vui lòng nhập mật khẩu");
            isValid = false;
        } else if (password.length() < 6) {
            passwordError.setValue("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        }

        return isValid;
    }
}
