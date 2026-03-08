package com.trototvn.trototandroid.di;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trototvn.trototandroid.data.remote.ApiClient;
import com.trototvn.trototandroid.data.remote.ApiService;
import com.trototvn.trototandroid.data.remote.AuthInterceptor;
import com.trototvn.trototandroid.data.remote.NetworkInterceptor;
import com.trototvn.trototandroid.data.remote.TokenAuthenticator;
import com.trototvn.trototandroid.utils.Constants;
import com.trototvn.trototandroid.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Hilt module for network dependencies
 */
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final int TIMEOUT_SECONDS = 60;

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    @Provides
    @Singleton
    public AuthInterceptor provideAuthInterceptor(@ApplicationContext Context context) {
        return new AuthInterceptor(context);
    }

    @Provides
    @Singleton
    public NetworkInterceptor provideNetworkInterceptor(@ApplicationContext Context context) {
        return new NetworkInterceptor(context);
    }

    @Provides
    @Singleton
    public TokenAuthenticator provideTokenAuthenticator(
            SessionManager sessionManager,
            javax.inject.Provider<ApiService> apiServiceProvider) {
        return new TokenAuthenticator(sessionManager, apiServiceProvider);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(
            AuthInterceptor authInterceptor,
            NetworkInterceptor networkInterceptor,
            HttpLoggingInterceptor loggingInterceptor,
            TokenAuthenticator tokenAuthenticator) {
        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(networkInterceptor)
                .addInterceptor(loggingInterceptor)
                .authenticator(tokenAuthenticator)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }

    @Provides
    @Singleton
    public com.trototvn.trototandroid.data.remote.ProfileApiService provideProfileApiService(Retrofit retrofit) {
        return retrofit.create(com.trototvn.trototandroid.data.remote.ProfileApiService.class);
    }

    @Provides
    @Singleton
    public com.trototvn.trototandroid.utils.SocketIOManager provideSocketIOManager(Gson gson) {
        return new com.trototvn.trototandroid.utils.SocketIOManager(gson);
    }
}
