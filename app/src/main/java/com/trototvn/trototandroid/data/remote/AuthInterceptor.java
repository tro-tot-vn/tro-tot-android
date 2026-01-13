package com.trototvn.trototandroid.data.remote;

import android.content.Context;

import androidx.annotation.NonNull;

import com.trototvn.trototandroid.utils.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Auth Interceptor - Add authorization token to requests
 */
public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder();

        // Add token if available
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        // Add other common headers
        requestBuilder.addHeader("Accept", "application/json");
        requestBuilder.addHeader("Content-Type", "application/json");

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
