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
import com.trototvn.trototandroid.databinding.FragmentRecommendedPostsBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RecommendedPostsFragment extends Fragment {

    private FragmentRecommendedPostsBinding binding;
    private HomeViewModel viewModel;
    private PostAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecommendedPostsBinding.inflate(inflater, container, false);
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
        if (viewModel.getRecommendations().getValue() == null) {
            viewModel.loadRecommendations();
        }
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(post -> {
            Bundle bundle = new Bundle();
            bundle.putInt("postId", post.getPostId());
            NavHostFragment.findNavController(requireParentFragment())
                    .navigate(R.id.postDetailFragment, bundle);
        });

        binding.rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecommendations.setAdapter(adapter);

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.loadRecommendations();
        });
    }

    private void setupObservers() {
        viewModel.getRecommendations().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.LOADING) {
                if (!binding.swipeRefreshLayout.isRefreshing() && adapter.getItemCount() == 0) {
                    binding.pbRecommendations.setVisibility(View.VISIBLE);
                }
            } else if (resource.getStatus() == Resource.Status.SUCCESS) {
                binding.pbRecommendations.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);

                if (resource.getData() != null) {
                    adapter.submitList(resource.getData());
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.pbRecommendations.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        viewModel.getHasMoreRecommendations().observe(getViewLifecycleOwner(), hasMore -> {
            binding.btnLoadMore.setVisibility(Boolean.TRUE.equals(hasMore) ? View.VISIBLE : View.GONE);
        });
    }

    private void setupClickListeners() {
        binding.btnLoadMore.setOnClickListener(v -> viewModel.loadMoreRecommendations());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
