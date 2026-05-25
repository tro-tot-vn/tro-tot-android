package com.trototvn.trototandroid.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.repository.ChatRepository;
import com.trototvn.trototandroid.databinding.ActivityMainBinding;
import com.trototvn.trototandroid.ui.auth.AuthActivity;
import com.trototvn.trototandroid.ui.splash.SplashActivity;
import com.trototvn.trototandroid.utils.SessionManager;
import com.trototvn.trototandroid.utils.SocketIOManager;

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
    SocketIOManager socketIOManager;

    @Inject
    ChatRepository chatRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupBottomNavigation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Initialize Notification Channel
        new com.trototvn.trototandroid.utils.NotificationHelper(this).createNotificationChannel();

        // Connect socket if logged in
        if (sessionManager.isLoggedIn()) {
            // 1. Bắt đầu kéo dữ liệu offline về DB
            chatRepository.performHandshakeSync();

            // 2. Bật kết nối Socket
            socketIOManager.connect(sessionManager.getUserId());

            // 3. Lắng nghe socket events globally
            chatRepository.observeIncomingMessages();
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            String conversationIdStr = intent.getStringExtra("conversationId");
            if (conversationIdStr != null) {
                try {
                    long conversationId = Long.parseLong(conversationIdStr);
                    String partnerName = intent.getStringExtra("partnerName");
                    if (partnerName == null)
                        partnerName = "Chat Partner";

                    Bundle bundle = new Bundle();
                    bundle.putLong("conversationId", conversationId);
                    bundle.putString("partnerName", partnerName);

                    if (navController != null) {
                        navController.navigate(R.id.chatDetailFragment, bundle);
                    }

                    // Remove the extra to prevent re-processing on configuration changes
                    intent.removeExtra("conversationId");
                } catch (NumberFormatException ignored) {
                }
            }

            // Handle post subscription notification click
            String postIdStr = intent.getStringExtra("postId");
            String type = intent.getStringExtra("type");
            if ("new_post_subscription".equals(type) && postIdStr != null) {
                try {
                    int postId = Integer.parseInt(postIdStr);
                    Bundle bundle = new Bundle();
                    bundle.putInt("postId", postId);

                    if (navController != null) {
                        navController.navigate(R.id.postDetailFragment, bundle);
                    }

                    // Remove the extras to prevent re-processing on configuration changes
                    intent.removeExtra("postId");
                    intent.removeExtra("type");
                } catch (NumberFormatException ignored) {
                }
            }
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

            // Icons use @color/icon_fill in vectors; avoid theme-attr tint (broken on API 35/36)
            binding.bottomNavigation.setItemIconTintList(null);

            // Optional: Handle reselection (prevent fragment recreation)
            binding.bottomNavigation.setOnItemReselectedListener(item -> {
                // Do nothing on reselection
            });
        }
    }

    public void logout() {
        chatRepository.stopObservingIncomingMessages();
        socketIOManager.disconnect();
        
        // Clear Room database data
        chatRepository.clearAllData()
                .subscribe(
                        () -> android.util.Log.d("MainActivity", "Local database cleared on logout"),
                        throwable -> android.util.Log.e("MainActivity", "Failed to clear local database on logout", throwable)
                );

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
        if (chatRepository != null) {
            chatRepository.stopObservingIncomingMessages();
        }
        binding = null;
    }
}
