package com.trototvn.trototandroid.ui.auth.login;

import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.auth.LoginResponse;
import com.trototvn.trototandroid.data.repository.AuthRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

/**
 * LoginViewModel - Handles login logic and validation
 */
@HiltViewModel
public class LoginViewModel extends BaseViewModel {

    private final AuthRepository authRepository;

    // LiveData for login result
    private final MutableLiveData<Resource<LoginResponse>> loginResult = new MutableLiveData<>();

    // LiveData for validation errors
    private final MutableLiveData<String> identifierError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();

    @Inject
    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
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
        handleLoading(loginResult);

        // Call repository
        addDisposable(
                authRepository.login(identifier, password)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resource -> loginResult.setValue(resource),
                                throwable -> handleError(loginResult, throwable.getMessage())
                        )
        );
    }

    /**
     * Validate login input
     */
    private boolean validateInput(String identifier, String password) {
        boolean isValid = true;

        if (identifier == null || identifier.trim().isEmpty()) {
            identifierError.setValue("Vui lòng nhập số điện thoại hoặc email");
            isValid = false;
        }

        if (password == null || password.trim().isEmpty()) {
            passwordError.setValue("Vui lòng nhập mật khẩu");
            isValid = false;
        }

        return isValid;
    }
}
