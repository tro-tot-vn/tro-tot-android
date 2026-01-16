package com.trototvn.trototandroid.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.trototvn.trototandroid.data.model.ResponseData;
import com.trototvn.trototandroid.data.model.auth.RefreshTokenRequest;
import com.trototvn.trototandroid.data.model.auth.RefreshTokenResponse;
import com.trototvn.trototandroid.utils.SessionManager;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

/**
 * TokenAuthenticator - Handles automatic token refresh when API returns 401
 * Backend ONLY returns new accessToken, we must keep existing refreshToken
 */
public class TokenAuthenticator implements Authenticator {

    private final SessionManager sessionManager;
    private final Provider<ApiService> apiServiceProvider; // Use Provider to avoid circular dependency

    @Inject
    public TokenAuthenticator(SessionManager sessionManager, Provider<ApiService> apiServiceProvider) {
        this.sessionManager = sessionManager;
        this.apiServiceProvider = apiServiceProvider;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        // If we already tried to refresh and failed, give up
        if (responseCount(response) >= 2) {
            return null;
        }

        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null) {
            return null; // No refresh token available, let user login again
        }

        // Call refresh token API synchronously
        ApiService apiService = apiServiceProvider.get();
        Call<ResponseData<RefreshTokenResponse>> call = apiService.refreshToken(new RefreshTokenRequest(refreshToken));
        
        try {
            retrofit2.Response<ResponseData<RefreshTokenResponse>> refreshResponse = call.execute();

            if (refreshResponse.isSuccessful() 
                    && refreshResponse.body() != null 
                    && refreshResponse.body().getData() != null) {
                
                RefreshTokenResponse tokenResponse = refreshResponse.body().getData();
                String newAccessToken = tokenResponse.getAccessToken();
                
                // Save new accessToken BUT KEEP the same refreshToken
                // (Backend doesn't send new refreshToken, we reuse the old one)
                sessionManager.saveSession(
                        newAccessToken,
                        refreshToken,  // <-- KEEP existing refreshToken!
                        sessionManager.getUserId(),
                        sessionManager.getUserName(),
                        sessionManager.getUserEmail()
                );

                // Retry original request with new accessToken
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newAccessToken)
                        .build();
            } else {
                // Refresh failed (e.g., refresh token expired), logout user
                sessionManager.clearSession();
                return null;
            }
        } catch (Exception e) {
            // Network error during refresh
            return null;
        }
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
