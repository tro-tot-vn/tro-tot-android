package com.trototvn.trototandroid.ui.main.accountsettings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.databinding.FragmentAccountSettingsBinding;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * AccountSettingsFragment - Account settings hub
 * Shows account management options like change password
 */
@AndroidEntryPoint
public class AccountSettingsFragment extends Fragment {

    private FragmentAccountSettingsBinding binding;
    private AccountSettingsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AccountSettingsViewModel.class);

        setupUI();
    }

    private void setupUI() {
        // Back button
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Change Password button
        binding.btnChangePassword.setOnClickListener(v -> {
            Timber.d("Change password clicked");
            NavHostFragment.findNavController(this)
                    .navigate(R.id.changePasswordFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

