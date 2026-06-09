package com.trototvn.trototandroid.di;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trototvn.trototandroid.data.remote.ApiClient;
import com.trototvn.trototandroid.data.remote.ApiService;
import com.trototvn.trototandroid.data.remote.AuthInterceptor;
import com.trototvn.trototandroid.data.remote.NetworkInterceptor;
import com.trototvn.trototandroid.data.remote.ProfileApiService;
import com.trototvn.trototandroid.data.remote.TokenAuthenticator;
import com.trototvn.trototandroid.utils.Constants;
import com.trototvn.trototandroid.utils.SessionManager;
import com.trototvn.trototandroid.utils.SocketIOManager;

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
                .registerTypeAdapter(java.util.Date.class, new GsonDateAdapter())
                .create();
    }

    private static class GsonDateAdapter implements com.google.gson.JsonSerializer<java.util.Date>, com.google.gson.JsonDeserializer<java.util.Date> {
        private final java.text.SimpleDateFormat[] formats;

        public GsonDateAdapter() {
            formats = new java.text.SimpleDateFormat[]{
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            };
            for (java.text.SimpleDateFormat f : formats) {
                f.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
            }
        }

        @Override
        public com.google.gson.JsonElement serialize(java.util.Date src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
            synchronized (formats[0]) {
                return new com.google.gson.JsonPrimitive(formats[0].format(src));
            }
        }

        @Override
        public java.util.Date deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            String cleanStr = json.getAsString().trim();
            if (cleanStr.isEmpty()) {
                return null;
            }

            for (java.text.SimpleDateFormat format : formats) {
                try {
                    synchronized (format) {
                        return format.parse(cleanStr);
                    }
                } catch (Exception ignored) {}
            }

            try {
                long epochTime = Long.parseLong(cleanStr);
                if (epochTime > 0) {
                    if (epochTime < 10000000000L) {
                        return new java.util.Date(epochTime * 1000);
                    }
                    return new java.util.Date(epochTime);
                }
            } catch (NumberFormatException ignored) {}

            throw new com.google.gson.JsonParseException("Unparseable date: \"" + cleanStr + "\"");
        }
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
    public ProfileApiService provideProfileApiService(Retrofit retrofit) {
        return retrofit.create(ProfileApiService.class);
    }

    @Provides
    @Singleton
    public SocketIOManager provideSocketIOManager(Gson gson, SessionManager sessionManager) {
        return new SocketIOManager(gson, sessionManager);
    }
}
