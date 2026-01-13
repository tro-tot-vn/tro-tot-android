package com.trototvn.trototandroid.data.remote;

import android.content.Context;

import androidx.annotation.NonNull;

import com.trototvn.trototandroid.utils.NetworkUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Network Interceptor - Check internet connectivity before making requests
 */
public class NetworkInterceptor implements Interceptor {

    private final Context context;

    public NetworkInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if (!NetworkUtils.isConnected(context)) {
            throw new NoConnectivityException();
        }

        Request request = chain.request();
        return chain.proceed(request);
    }

    /**
     * Custom exception for no connectivity
     */
    public static class NoConnectivityException extends IOException {
        @Override
        public String getMessage() {
            return "No internet connection available";
        }
    }
}
