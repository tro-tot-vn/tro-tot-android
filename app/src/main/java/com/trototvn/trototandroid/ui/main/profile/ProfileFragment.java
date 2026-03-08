package com.trototvn.trototandroid.ui.main.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.databinding.FragmentProfileBinding;
import com.trototvn.trototandroid.ui.main.MainActivity;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * ProfileFragment - Main profile hub
 * Shows profile info and menu items for navigation
 */
@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private ProfileMenuAdapter menuAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupRecyclerView();
        setupObservers();

        // Load data
        viewModel.loadProfile();
        viewModel.loadCounts();
    }

    private void setupRecyclerView() {
        menuAdapter = new ProfileMenuAdapter(menuItem -> {
            // Handle menu item clicks
            Timber.d("Menu item clicked: %s", menuItem.getTitle());

            switch (itemTypeToProfileMenuItemType(menuItem.getType())) {
                case LOGOUT:
                    showLogoutConfirmation();
                    break;
                case EDIT_PROFILE:
                    // TODO: Implement navigation
                    break;
                case SETTINGS:
                    // TODO: Implement navigation
                    break;
                // Add other types as needed
            }
        });

        binding.rvMenuItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMenuItems.setAdapter(menuAdapter);
    }

    private ProfileMenuItem.ItemType itemTypeToProfileMenuItemType(ProfileMenuItem.ItemType type) {
        return type;
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setupObservers() {
        // Observe profile data
        viewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.LOADING) {
                binding.pbLoading.setVisibility(View.VISIBLE);
            } else {
                binding.pbLoading.setVisibility(View.GONE);

                if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                    updateProfile(resource.getData());
                } else if (resource.getStatus() == Resource.Status.ERROR) {
                    Timber.e("Error loading profile: %s", resource.getMessage());
                }
            }
        });

        // Observe counts and update menu
        viewModel.getSavedPostsCount().observe(getViewLifecycleOwner(), count -> updateMenuCounts());
        viewModel.getHistoryCount().observe(getViewLifecycleOwner(), count -> updateMenuCounts());
        viewModel.getSubscriptionsCount().observe(getViewLifecycleOwner(), count -> updateMenuCounts());
    }

    private void updateProfile(com.trototvn.trototandroid.data.model.profile.CustomerProfile profile) {
        // Update UI
        binding.tvName.setText(profile.getFullName());
        binding.tvEmail.setText(profile.getEmail());

        if (profile.getJoinedAt() != null) {
            String memberSince = viewModel.getMemberSinceDuration(profile.getJoinedAt());
            binding.tvMemberSince.setText(getString(R.string.member_since, memberSince));
        }

        // TODO: Load avatar with Glide
        // Glide.with(this).load(profile.getAvatar()).into(binding.ivAvatar);
    }

    private void updateMenuCounts() {
        Integer savedCount = viewModel.getSavedPostsCount().getValue();
        Integer historyCount = viewModel.getHistoryCount().getValue();
        Integer subsCount = viewModel.getSubscriptionsCount().getValue();

        // Create menu items with counts
        java.util.List<ProfileMenuItem> menuItems = new java.util.ArrayList<>();

        // Edit Profile
        menuItems.add(new ProfileMenuItem(
                R.drawable.ic_pencil,
                getString(R.string.edit_profile),
                null,
                ProfileMenuItem.ItemType.EDIT_PROFILE));

        // Saved Posts
        menuItems.add(new ProfileMenuItem(
                R.drawable.ic_bookmark,
                getString(R.string.saved_posts),
                savedCount != null && savedCount > 0 ? savedCount + " tin" : null,
                ProfileMenuItem.ItemType.SAVED_POSTS));

        // View History
        menuItems.add(new ProfileMenuItem(
                R.drawable.ic_time_past,
                getString(R.string.view_history),
                historyCount != null && historyCount > 0 ? historyCount + " tin" : null,
                ProfileMenuItem.ItemType.VIEW_HISTORY));

        // Subscriptions
        menuItems.add(new ProfileMenuItem(
                R.drawable.ic_land_layer_location,
                getString(R.string.subscriptions),
                subsCount != null && subsCount > 0 ? subsCount + " khu vực" : null,
                ProfileMenuItem.ItemType.SUBSCRIPTIONS));

        // Settings
        menuItems.add(new ProfileMenuItem(
                R.drawable.ic_settings,
                getString(R.string.account_settings),
                null,
                ProfileMenuItem.ItemType.SETTINGS));

        // Logout
        menuItems.add(new ProfileMenuItem(
                R.drawable.ic_user_logout,
                getString(R.string.logout),
                null,
                ProfileMenuItem.ItemType.LOGOUT));

        menuAdapter.submitList(menuItems);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
