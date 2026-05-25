package com.trototvn.trototandroid.di;

import android.content.Context;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * MyAppGlideModule configures Glide to use OkHttp instead of default HttpURLConnection
 * utilizing a clean OkHttpClient configuration without auth interceptor and JSON content types.
 */
@GlideModule
public final class MyAppGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient cleanClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        registry.replace(
                GlideUrl.class,
                InputStream.class,
                new OkHttpUrlLoader.Factory((okhttp3.Call.Factory) cleanClient)
        );
    }
}
