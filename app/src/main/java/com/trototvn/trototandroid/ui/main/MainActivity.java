package com.trototvn.trototandroid.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.databinding.ActivityMainBinding;
import com.trototvn.trototandroid.ui.splash.SplashActivity;

import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;

import com.trototvn.trototandroid.di.GlideApp;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

/**
 * MainActivity - Main app container with bottom navigation
 * Hosts Home, Search, MyPosts, and Profile fragments
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private MainViewModel viewModel;

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable hideBannerRunnable;
    private boolean isBannerShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getLogoutCompleted().observe(this, completed -> {
            if (Boolean.TRUE.equals(completed)) {
                Intent intent = new Intent(this, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Initialize session and socket connection inside ViewModel
        viewModel.initSessionSync();

        viewModel.getInAppNotificationEvent().observe(this, event -> {
            if (event != null) {
                showInAppBanner(event);
                viewModel.clearInAppNotificationEvent();
            }
        });

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
        viewModel.logout();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void showInAppBanner(com.trototvn.trototandroid.data.repository.ChatRepository.InAppNotificationEvent event) {
        if (binding == null) return;

        binding.notificationTitle.setText(event.partnerName);
        binding.notificationBody.setText(event.messageContent);

        // Load avatar using Glide
        GlideApp.with(this)
                .load(event.partnerAvatar)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .circleCrop()
                .into(binding.notificationAvatar);

        // Click on banner to open chat
        binding.inAppNotificationBanner.setOnClickListener(v -> {
            hideInAppBanner(true);
            if (navController != null) {
                Bundle bundle = new Bundle();
                bundle.putLong("conversationId", event.conversationId);
                bundle.putString("partnerName", event.partnerName);
                navController.navigate(R.id.chatDetailFragment, bundle);
            }
        });

        // Click close button to hide
        binding.notificationCloseBtn.setOnClickListener(v -> hideInAppBanner(true));

        if (!isBannerShowing) {
            isBannerShowing = true;
            binding.inAppNotificationBanner.setVisibility(View.VISIBLE);
            binding.inAppNotificationBanner.setAlpha(0f);
            binding.inAppNotificationBanner.setTranslationY(-300f);
            binding.inAppNotificationBanner.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // Auto hide after 4 seconds
        if (hideBannerRunnable != null) {
            bannerHandler.removeCallbacks(hideBannerRunnable);
        }
        hideBannerRunnable = () -> hideInAppBanner(true);
        bannerHandler.postDelayed(hideBannerRunnable, 4000);
    }

    private void hideInAppBanner(boolean animate) {
        if (binding == null || !isBannerShowing) return;

        isBannerShowing = false;
        if (hideBannerRunnable != null) {
            bannerHandler.removeCallbacks(hideBannerRunnable);
            hideBannerRunnable = null;
        }

        if (animate) {
            binding.inAppNotificationBanner.animate()
                    .alpha(0f)
                    .translationY(-300f)
                    .setDuration(250)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        if (binding != null) {
                            binding.inAppNotificationBanner.setVisibility(View.GONE);
                        }
                    })
                    .start();
        } else {
            binding.inAppNotificationBanner.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        if (hideBannerRunnable != null) {
            bannerHandler.removeCallbacks(hideBannerRunnable);
        }
        super.onDestroy();
        binding = null;
    }
}
