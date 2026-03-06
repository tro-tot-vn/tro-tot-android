package com.trototvn.trototandroid.data.remote;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trototvn.trototandroid.utils.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API Client - Singleton Retrofit instance
 */
public class ApiClient {

    private static Retrofit retrofit;

    private ApiClient() {
        // Private constructor
    }

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    retrofit = buildRetrofit(context);
                }
            }
        }
        return retrofit;
    }

    private static Retrofit buildRetrofit(Context context) {
        // Gson configuration
        Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        // OkHttp client
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(context))
                .addInterceptor(new NetworkInterceptor(context))
                .addInterceptor(createLoggingInterceptor())
                .build();

        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // RxJava support
                .build();
    }

    private static HttpLoggingInterceptor createLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    /**
     * Create API service
     */
    public static <T> T createService(Context context, Class<T> serviceClass) {
        return getClient(context).create(serviceClass);
    }
}
