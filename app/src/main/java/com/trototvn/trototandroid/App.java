package com.trototvn.trototandroid;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

/**
 * Application class with Hilt and Timber initialization
 */
@HiltAndroidApp
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Timber.d("App initialized");
    }
}
