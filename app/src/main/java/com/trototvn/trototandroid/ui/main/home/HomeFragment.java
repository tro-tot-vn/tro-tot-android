package com.trototvn.trototandroid.ui.main.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.databinding.FragmentHomeBinding;
import com.trototvn.trototandroid.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * HomeFragment - Main feed with ViewPager2 for Latest posts and Recommendations tabs
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Inject
    SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel here so child fragments can share it
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupViewPagerAndTabs();
    }

    private void setupViewPagerAndTabs() {
        boolean isLoggedIn = sessionManager.isLoggedIn();

        HomePagerAdapter pagerAdapter = new HomePagerAdapter(this, isLoggedIn);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.latest_posts);
            } else if (position == 1) {
                tab.setText(R.string.recommendations_for_you);
            }
        }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
