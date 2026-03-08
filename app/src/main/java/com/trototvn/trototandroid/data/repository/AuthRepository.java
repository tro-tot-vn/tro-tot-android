package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.auth.Account;
import com.trototvn.trototandroid.data.model.auth.LoginRequest;
import com.trototvn.trototandroid.data.model.auth.LoginResponse;
import com.trototvn.trototandroid.data.model.auth.RegisterRequest;
import com.trototvn.trototandroid.data.model.auth.RegisterResponse;
import com.trototvn.trototandroid.data.model.auth.Token;
import com.trototvn.trototandroid.data.remote.ApiService;
import com.trototvn.trototandroid.utils.ErrorHandler;
import com.trototvn.trototandroid.utils.SessionManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
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
    public Single<Resource<RegisterResponse>> register(RegisterRequest request) {
        return apiService.register(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<RegisterResponse>error(response.getMessage(), null);
                    }
                })
                .onErrorResumeNext(new Function<Throwable, Single<Resource<RegisterResponse>>>() {
                    @Override
                    public Single<Resource<RegisterResponse>> apply(Throwable throwable) {
                        Timber.e(throwable, "Register error");
                        Resource<RegisterResponse> errorResource = Resource.error(throwable.getMessage(), null);
                        return Single.just(errorResource);
                    }
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
}
