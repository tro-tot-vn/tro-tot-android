package com.trototvn.trototandroid.ui.main.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.databinding.FragmentLatestPostsBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LatestPostsFragment extends Fragment {

    private FragmentLatestPostsBinding binding;
    private HomeViewModel viewModel;
    private PostAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLatestPostsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Share ViewModel with parent Fragment (HomeFragment)
        viewModel = new ViewModelProvider(requireParentFragment()).get(HomeViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        // Initial load
        if (viewModel.getLatestPosts().getValue() == null) {
            viewModel.loadLatestPosts();
        }
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(post -> {
            Bundle bundle = new Bundle();
            bundle.putInt("postId", post.getPostId());
            // Since we are in ViewPager, we navigate from parent Fragment
            NavHostFragment.findNavController(requireParentFragment())
                    .navigate(R.id.postDetailFragment, bundle);
        });

        binding.rvLatestPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLatestPosts.setAdapter(adapter);

        // SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.loadLatestPosts();
        });
    }

    private void setupObservers() {
        viewModel.getLatestPosts().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.LOADING) {
                // Only show progress bar if not swipe refreshing
                if (!binding.swipeRefreshLayout.isRefreshing() && adapter.getItemCount() == 0) {
                    binding.pbLatestPosts.setVisibility(View.VISIBLE);
                }
                binding.layoutLatestError.setVisibility(View.GONE);
            } else if (resource.getStatus() == Resource.Status.SUCCESS) {
                binding.pbLatestPosts.setVisibility(View.GONE);
                binding.layoutLatestError.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);

                if (resource.getData() != null) {
                    adapter.submitList(resource.getData());
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.pbLatestPosts.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                if (adapter.getItemCount() == 0) {
                    binding.layoutLatestError.setVisibility(View.VISIBLE);
                    binding.tvLatestError.setText(resource.getMessage());
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.btnRetryLatest.setOnClickListener(v -> viewModel.loadLatestPosts());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
