package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.auth.Account;
import com.trototvn.trototandroid.data.model.auth.LoginRequest;
import com.trototvn.trototandroid.data.model.auth.LoginResponse;
import com.trototvn.trototandroid.data.model.auth.RegisterRequest;
import com.trototvn.trototandroid.data.model.auth.RegisterResponse;
import com.trototvn.trototandroid.data.model.auth.Token;
import com.trototvn.trototandroid.data.model.auth.OTPRequest;
import com.trototvn.trototandroid.data.model.auth.OTPResponse;
import com.trototvn.trototandroid.data.model.auth.ResetPasswordRequest;
import com.trototvn.trototandroid.data.model.auth.VerifyOTPRequest;
import com.trototvn.trototandroid.data.model.auth.VerifyOTPResponse;
import com.trototvn.trototandroid.data.remote.ApiService;
import com.trototvn.trototandroid.utils.ErrorHandler;
import com.trototvn.trototandroid.utils.SessionManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * AuthRepository - Handles authentication business logic with clean code
 * practices
 */
@Singleton
public class AuthRepository {

    private final ApiService apiService;
    private final SessionManager sessionManager;

    @Inject
    public AuthRepository(ApiService apiService, SessionManager sessionManager) {
        this.apiService = apiService;
        this.sessionManager = sessionManager;
    }

    /**
     * Login with identifier (phone or email) and password
     */
    public Single<Resource<LoginResponse>> login(String identifier, String password) {
        LoginRequest request = new LoginRequest(identifier, password);

        return apiService.login(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        // Save token and user info to session
                        LoginResponse data = response.getData();
                        if (data.getToken() != null && data.getAccount() != null) {
                            Timber.i("Login successful for user: %s", data.getAccount().getEmail());
                            saveUserSession(data.getAccount(),
                                    data.getToken().getAccessToken(),
                                    data.getToken().getRefreshToken());
                        }
                        return Resource.success(data);
                    } else {
                        return Resource.<LoginResponse>error(response.getMessage(), null);
                    }
                })
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable, "Login error");
                    String userMessage = ErrorHandler.getErrorMessage(throwable);
                    return Single.just(Resource.error(userMessage, null));
                });
    }

    /**
     * Register new account
     */
    public Single<Resource<String>> register(RegisterRequest request) {
        return apiService.register(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<String>error(response.getMessage(), null);
                    }
                })
                .onErrorResumeNext(new Function<Throwable, Single<Resource<String>>>() {
                    @Override
                    public Single<Resource<String>> apply(Throwable throwable) {
                        Timber.e(throwable, "Register error");
                        String userMessage = ErrorHandler.getErrorMessage(throwable);
                        Resource<String> errorResource = Resource.error(userMessage, null);
                        return Single.just(errorResource);
                    }
                });
    }

    /**
     * Send OTP for registration
     */
    public Single<Resource<OTPResponse>> sendOtpRegister(String email) {
        return apiService.sendOtpRegister(new OTPRequest(email))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<OTPResponse>error(response.getMessage(), null);
                    }
                })
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable, "Send OTP error");
                    return Single.just(Resource.error(ErrorHandler.getErrorMessage(throwable), null));
                });
    }

    /**
     * Verify OTP for registration
     */
    public Single<Resource<Void>> verifyOtpRegister(String email, String otp) {
        return apiService.verifyOtpRegister(new VerifyOTPRequest(email, otp))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable, "Verify OTP error");
                    return Single.just(Resource.error(ErrorHandler.getErrorMessage(throwable), null));
                });
    }

    /**
     * Forgot password - Send OTP
     */
    public Single<Resource<OTPResponse>> forgotPassword(String email) {
        return apiService.forgotPassword(new OTPRequest(email))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<OTPResponse>error(response.getMessage(), null);
                    }
                })
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable, "Forgot password error");
                    return Single.just(Resource.error(ErrorHandler.getErrorMessage(throwable), null));
                });
    }

    /**
     * Verify OTP for forgot password
     */
    public Single<Resource<VerifyOTPResponse>> verifyOtp(String email, String otp) {
        return apiService.verifyOtp(new VerifyOTPRequest(email, otp))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<VerifyOTPResponse>error(response.getMessage(), null);
                    }
                })
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable, "Verify OTP forgot password error");
                    return Single.just(Resource.error(ErrorHandler.getErrorMessage(throwable), null));
                });
    }

    /**
     * Reset password
     */
    public Single<Resource<Void>> resetPassword(String resetToken, String newPassword) {
        return apiService.resetPassword(new ResetPasswordRequest(resetToken, newPassword))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable, "Reset password error");
                    return Single.just(Resource.error(ErrorHandler.getErrorMessage(throwable), null));
                });
    }

    /**
     * Save user session after login
     */
    private void saveUserSession(Account account, String accessToken, String refreshToken) {
        sessionManager.saveSession(
                accessToken,
                refreshToken,
                account.getId(),
                account.getFullName(),
                account.getEmail());
    }

    /**
     * Clear session (logout)
     */
    public void clearSession() {
        sessionManager.clearSession();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    /**
     * Sync FCM Token to Backend
     */
    public Completable registerFcmToken(String token) {
        if (token == null || token.isEmpty()) {
            return Completable.complete();
        }

        com.trototvn.trototandroid.data.model.notification.FcmTokenRequest request =
                new com.trototvn.trototandroid.data.model.notification.FcmTokenRequest(token);

        return apiService.registerFcmToken(request)
                .subscribeOn(Schedulers.io())
                .ignoreElement()
                .onErrorComplete(); // Fire-and-forget, swallow error if it fails
    }

    /**
     * Unregister FCM Token from Backend on Logout
     */
    public Completable unregisterFcmToken(String token) {
        if (token == null || token.isEmpty()) {
            return Completable.complete();
        }

        com.trototvn.trototandroid.data.model.notification.FcmTokenRequest request =
                new com.trototvn.trototandroid.data.model.notification.FcmTokenRequest(token);

        return apiService.unregisterFcmToken(request)
                .subscribeOn(Schedulers.io())
                .ignoreElement()
                .onErrorComplete(); // Fire-and-forget, swallow error if it fails
    }
}
