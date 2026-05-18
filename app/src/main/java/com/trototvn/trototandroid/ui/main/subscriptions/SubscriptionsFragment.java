package com.trototvn.trototandroid.ui.main.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.databinding.FragmentSubscriptionsBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SubscriptionsFragment extends Fragment {

    private FragmentSubscriptionsBinding binding;
    private SubscriptionsViewModel viewModel;
    private SubscriptionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSubscriptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SubscriptionsViewModel.class);

        setupUI();
        setupObservers();
    }

    private void setupUI() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        adapter = new SubscriptionAdapter(subscription -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xoá khu vực")
                    .setMessage("Bạn có chắc chắn muốn ngừng theo dõi " + subscription.getDisplayText() + " không?")
                    .setPositiveButton("Xoá", (dialog, which) -> viewModel.removeSubscription(subscription))
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
        binding.rvSubscriptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSubscriptions.setAdapter(adapter);

        binding.btnAddSubscription.setOnClickListener(v -> {
            AddSubscriptionBottomSheet bottomSheet = new AddSubscriptionBottomSheet(
                (city, district) -> viewModel.addSubscription(city, district)
            );
            bottomSheet.show(getChildFragmentManager(), "AddSubscriptionBottomSheet");
        });
    }

    private void setupObservers() {
        viewModel.getSubscriptions().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.getStatus() == Resource.Status.SUCCESS) {
                boolean isEmpty = resource.getData() == null || resource.getData().isEmpty();
                binding.tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.rvSubscriptions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                adapter.submitList(resource.getData());
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getAddSubscriptionResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (resource != null && resource.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Đã thêm khu vực theo dõi", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getRemoveSubscriptionResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (resource != null && resource.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Đã xoá khu vực theo dõi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

