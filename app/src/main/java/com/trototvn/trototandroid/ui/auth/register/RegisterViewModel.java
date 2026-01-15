package com.trototvn.trototandroid.ui.auth.register;

import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.auth.RegisterRequest;
import com.trototvn.trototandroid.data.model.auth.RegisterResponse;
import com.trototvn.trototandroid.data.repository.AuthRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;

import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

/**
 * RegisterViewModel - Handles registration logic and validation
 */
@HiltViewModel
public class RegisterViewModel extends BaseViewModel {

    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
    private static final Pattern PHONE_PATTERN = Pattern
            .compile("^0[0-9]{9}$"); // Vietnamese phone: 0 + 9 digits

    private final AuthRepository authRepository;

    // LiveData for register result
    private final MutableLiveData<Resource<RegisterResponse>> registerResult = new MutableLiveData<>();

    // LiveData for validation errors
    private final MutableLiveData<String> phoneError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> firstNameError = new MutableLiveData<>();
    private final MutableLiveData<String> lastNameError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();

    @Inject
    public RegisterViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public MutableLiveData<Resource<RegisterResponse>> getRegisterResult() {
        return registerResult;
    }

    public MutableLiveData<String> getPhoneError() {
        return phoneError;
    }

    public MutableLiveData<String > getEmailError() {
        return emailError;
    }

    public MutableLiveData<String> getFirstNameError() {
        return firstNameError;
    }

    public MutableLiveData<String> getLastNameError() {
        return lastNameError;
    }

    public MutableLiveData<String> getPasswordError() {
        return passwordError;
    }

    /**
     * Perform registration
     */
    public void register(String phone, String email, String firstName, String lastName,
                        String birthday, String gender, String password,
                        String currentCity, String currentDistrict, String currentJob) {
        // Clear previous errors
        clearErrors();

        // Validate input
        if (!validateInput(phone, email, firstName, lastName, password)) {
            return;
        }

        // Show loading
        handleLoading(registerResult);

        // Create request
        RegisterRequest request = new RegisterRequest(
                phone, email, firstName, lastName,
                birthday, gender, password,
                currentCity, currentDistrict, currentJob
        );

        // Call repository
        addDisposable(
                authRepository.register(request)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resource -> registerResult.setValue(resource),
                                throwable -> handleError(registerResult, throwable.getMessage())
                        )
        );
    }

    /**
     * Validate registration input
     */
    private boolean validateInput(String phone, String email, String firstName, 
                                  String lastName, String password) {
        boolean isValid = true;

        // Validate phone
        if (phone == null || phone.trim().isEmpty()) {
            phoneError.setValue("Vui lòng nhập số điện thoại");
            isValid = false;
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            phoneError.setValue("Số điện thoại không hợp lệ (10 chữ số, bắt đầu bằng 0)");
            isValid = false;
        }

        // Validate email
        if (email == null || email.trim().isEmpty()) {
            emailError.setValue("Vui lòng nhập email");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailError.setValue("Email không hợp lệ");
            isValid = false;
        }

        // Validate firstName
        if (firstName == null || firstName.trim().isEmpty()) {
            firstNameError.setValue("Vui lòng nhập họ");
            isValid = false;
        } else if (firstName.trim().length() < 2) {
            firstNameError.setValue("Họ phải có ít nhất 2 ký tự");
            isValid = false;
        }

        // Validate lastName
        if (lastName == null || lastName.trim().isEmpty()) {
            lastNameError.setValue("Vui lòng nhập tên");
            isValid = false;
        } else if (lastName.trim().length() < 2) {
            lastNameError.setValue("Tên phải có ít nhất 2 ký tự");
            isValid = false;
        }

        // Validate password
        if (password == null || password.trim().isEmpty()) {
            passwordError.setValue("Vui lòng nhập mật khẩu");
            isValid = false;
        } else if (password.length() < 6) {
            passwordError.setValue("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Clear all validation errors
     */
    private void clearErrors() {
        phoneError.setValue(null);
        emailError.setValue(null);
        firstNameError.setValue(null);
        lastNameError.setValue(null);
        passwordError.setValue(null);
    }
}
