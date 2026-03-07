package com.trototvn.trototandroid.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.databinding.ActivityMainBinding;
import com.trototvn.trototandroid.ui.auth.AuthActivity;
import com.trototvn.trototandroid.ui.splash.SplashActivity;
import com.trototvn.trototandroid.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * MainActivity - Main app container with bottom navigation
 * Hosts Home, Search, MyPosts, and Profile fragments
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Inject
    SessionManager sessionManager;

    @Inject
    com.trototvn.trototandroid.utils.SocketIOManager socketIOManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupBottomNavigation();

        // Connect socket if logged in
        if (sessionManager.isLoggedIn()) {
            socketIOManager.connect(sessionManager.getUserId());
        }
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
    }

    private void setupBottomNavigation() {
        if (navController != null) {
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            // Optional: Handle reselection (prevent fragment recreation)
            binding.bottomNavigation.setOnItemReselectedListener(item -> {
                // Do nothing on reselection
            });
        }
    }

    /**
     * Call this from fragments to logout
     */
    public void logout() {
        socketIOManager.disconnect();
        sessionManager.clearSession();

        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
