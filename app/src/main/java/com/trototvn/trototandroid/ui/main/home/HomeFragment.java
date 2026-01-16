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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.databinding.FragmentHomeBinding;
import com.trototvn.trototandroid.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * HomeFragment - Main feed with latest posts and recommendations
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private PostAdapter latestPostsAdapter;
    private PostAdapter recommendationsAdapter;

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
        
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        setupRecyclerViews();
        setupObservers();
        setupClickListeners();
        
        // Load data
        viewModel.loadLatestPosts();
        
        // Load recommendations only if logged in
        if (sessionManager.isLoggedIn()) {
            viewModel.loadRecommendations();
            binding.layoutRecommendations.setVisibility(View.VISIBLE);
        } else {
            binding.layoutRecommendations.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerViews() {
        // Latest Posts RecyclerView
        latestPostsAdapter = new PostAdapter(post -> {
            // TODO Session 2: Implement navigation to PostDetailFragment
            Timber.d("Post clicked - ID: %d, Title: %s", post.getPostId(), post.getTitle());
        });
        
        binding.rvLatestPosts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvLatestPosts.setAdapter(latestPostsAdapter);
        binding.rvLatestPosts.setNestedScrollingEnabled(false);
        
        // Recommendations RecyclerView
        recommendationsAdapter = new PostAdapter(post -> {
            // TODO Session 2: Implement navigation to PostDetailFragment
            Timber.d("Recommendation clicked - ID: %d", post.getPostId());
        });
        
        binding.rvRecommendations.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvRecommendations.setAdapter(recommendationsAdapter);
        binding.rvRecommendations.setNestedScrollingEnabled(false);
    }

    private void setupObservers() {
        // Latest Posts Observer
        viewModel.getLatestPosts().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.LOADING) {
                binding.pbLatestPosts.setVisibility(View.VISIBLE);
                binding.rvLatestPosts.setVisibility(View.GONE);
                binding.layoutLatestError.setVisibility(View.GONE);
            } else if (resource.getStatus() == Resource.Status.SUCCESS) {
                binding.pbLatestPosts.setVisibility(View.GONE);
                binding.rvLatestPosts.setVisibility(View.VISIBLE);
                binding.layoutLatestError.setVisibility(View.GONE);
                
                if (resource.getData() != null) {
                    latestPostsAdapter.submitList(resource.getData());
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.pbLatestPosts.setVisibility(View.GONE);
                binding.rvLatestPosts.setVisibility(View.GONE);
                binding.layoutLatestError.setVisibility(View.VISIBLE);
                binding.tvLatestError.setText(resource.getMessage());
            }
        });
        
        // Recommendations Observer
        viewModel.getRecommendations().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.LOADING) {
                binding.pbRecommendations.setVisibility(View.VISIBLE);
                binding.rvRecommendations.setVisibility(View.GONE);
            } else if (resource.getStatus() == Resource.Status.SUCCESS) {
                binding.pbRecommendations.setVisibility(View.GONE);
                binding.rvRecommendations.setVisibility(View.VISIBLE);
                
                if (resource.getData() != null) {
                    recommendationsAdapter.submitList(resource.getData());
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.pbRecommendations.setVisibility(View.GONE);
                binding.rvRecommendations.setVisibility(View.VISIBLE);
            }
        });
        
        // Load More visibility
        viewModel.getHasMoreRecommendations().observe(getViewLifecycleOwner(), hasMore -> {
            binding.btnLoadMore.setVisibility(Boolean.TRUE.equals(hasMore) ? View.VISIBLE : View.GONE);
        });
    }

    private void setupClickListeners() {
        // Retry Latest Posts
        binding.btnRetryLatest.setOnClickListener(v -> viewModel.loadLatestPosts());
        
        // Load More Recommendations
        binding.btnLoadMore.setOnClickListener(v -> viewModel.loadMoreRecommendations());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
