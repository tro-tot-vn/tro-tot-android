package com.trototvn.trototandroid.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.ui.auth.AuthActivity;
import com.trototvn.trototandroid.ui.main.MainActivity;
import com.trototvn.trototandroid.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Splash screen - App entry point
 * Checks authentication status and routes to appropriate screen
 */
@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {

    @Inject
    SessionManager sessionManager;

    private static final int SPLASH_DELAY = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay before navigation
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        Intent intent;
        
        if (sessionManager.isLoggedIn()) {
            // User is logged in → go to MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // User not logged in → go to AuthActivity
            intent = new Intent(this, AuthActivity.class);
        }
        
        startActivity(intent);
        finish(); // Remove splash from back stack
    }
}
