package com.trototvn.trototandroid.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.databinding.ActivityAdminBinding;
import com.trototvn.trototandroid.ui.splash.SplashActivity;
import com.trototvn.trototandroid.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * AdminActivity - container for the admin / moderation area.
 * Hosts Dashboard, Post Review, Moderators, Reports and Profile via admin_nav_graph.
 * Manager-only destinations (Moderators) are hidden from the bottom nav for non-Managers;
 * the backend enforces the same scopes server-side.
 */
@AndroidEntryPoint
public class AdminActivity extends AppCompatActivity {

    @Inject
    SessionManager sessionManager;

    @Inject
    com.trototvn.trototandroid.data.repository.AuthRepository authRepository;

    private ActivityAdminBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        applyRoleVisibility();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.admin_nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.adminBottomNavigation, navController);
            binding.adminBottomNavigation.setOnItemReselectedListener(item -> {
                // No-op on reselection
            });
        }
    }

    /** Hide Manager-only entries for plain moderators. */
    private void applyRoleVisibility() {
        if (!sessionManager.isManager()) {
            Menu menu = binding.adminBottomNavigation.getMenu();
            if (menu.findItem(R.id.adminModeratorsFragment) != null) {
                menu.findItem(R.id.adminModeratorsFragment).setVisible(false);
            }
        }
    }

    /** Clear the session and return to the splash/auth flow. */
    public void logout() {
        String fcmToken = sessionManager.getFcmToken();
        if (fcmToken != null && !fcmToken.isEmpty()) {
            // Đẩy unregister token lên io thread và sau khi xong (hoặc lỗi) thì logout
            io.reactivex.rxjava3.disposables.Disposable d = authRepository.unregisterFcmToken(fcmToken)
                    .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                    .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                    .doFinally(this::performLocalLogout)
                    .subscribe(
                            () -> timber.log.Timber.d("Admin FCM unregister success"),
                            throwable -> timber.log.Timber.e(throwable, "Admin FCM unregister failed")
                    );
        } else {
            performLocalLogout();
        }
    }

    private void performLocalLogout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return (navController != null && navController.navigateUp()) || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
