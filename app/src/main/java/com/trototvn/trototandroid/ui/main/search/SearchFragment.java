package com.trototvn.trototandroid.ui.main.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.databinding.FragmentSearchBinding;
import com.trototvn.trototandroid.ui.main.home.PostAdapter;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * SearchFragment - Search and filter posts
 */
@AndroidEntryPoint
public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private PostAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        
        setupSearchBar();
        setupRecyclerView();
        setupFilterChips();
        setupObservers();
        setupClickListeners();
        
        // Show empty state initially
        showEmptyState();
    }

    private void setupSearchBar() {
        // Set up text change listener
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle search action
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(post -> {
            // TODO: Navigate to detail & log click if searchLogId exists
            Timber.d("Search result clicked: %s (logId: %s)", 
                    post.getTitle(), viewModel.getSearchLogId());
        });
        
        binding.rvSearchResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvSearchResults.setAdapter(adapter);
        binding.rvSearchResults.setNestedScrollingEnabled(false);
    }

    private void setupFilterChips() {
        // Location chip
        binding.chipLocation.setOnClickListener(v -> {
            // TODO: Show location filter dialog
            Timber.d("Location filter clicked");
        });

        // Price chip
        binding.chipPrice.setOnClickListener(v -> {
            // TODO: Show price range filter dialog
            Timber.d("Price filter clicked");
        });

        // Acreage chip
        binding.chipAcreage.setOnClickListener(v -> {
            // TODO: Show acreage range filter dialog
            Timber.d("Acreage filter clicked");
        });

        // Interior chip
        binding.chipInterior.setOnClickListener(v -> {
            // TODO: Show interior condition filter dialog
            Timber.d("Interior filter clicked");
        });
    }

    private void setupObservers() {
        // Search results observer
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.LOADING) {
                showLoading();
            } else if (resource.getStatus() == Resource.Status.SUCCESS) {
                hideLoading();
                if (resource.getData() != null && !resource.getData().isEmpty()) {
                    showResults();
                    adapter.submitList(resource.getData());
                } else {
                    showError("Không tìm thấy kết quả");
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                hideLoading();
                showError(resource.getMessage());
            }
        });

        // Pagination observer
        viewModel.getHasMorePages().observe(getViewLifecycleOwner(), hasMore -> {
            binding.btnLoadMore.setVisibility(Boolean.TRUE.equals(hasMore) ? View.VISIBLE : View.GONE);
        });

        // Filter state observers (for chip checked state)
        viewModel.getSelectedCity().observe(getViewLifecycleOwner(), city -> {
            binding.chipLocation.setChecked(city != null);
            updateClearFiltersVisibility();
        });

        viewModel.getPriceMin().observe(getViewLifecycleOwner(), min -> {
            binding.chipPrice.setChecked(min != null || viewModel.getPriceMax().getValue() != null);
            updateClearFiltersVisibility();
        });

        viewModel.getAcreageMin().observe(getViewLifecycleOwner(), min -> {
            binding.chipAcreage.setChecked(min != null || viewModel.getAcreageMax().getValue() != null);
            updateClearFiltersVisibility();
        });
    }

    private void setupClickListeners() {
        // Clear filters
        binding.btnClearFilters.setOnClickListener(v -> {
            viewModel.clearFilters();
            // Re-execute search if there's a query
            if (viewModel.getQuery().getValue() != null && 
                !viewModel.getQuery().getValue().trim().isEmpty()) {
                viewModel.search();
            }
        });

        // Load more
        binding.btnLoadMore.setOnClickListener(v -> viewModel.loadNextPage());

        // Retry
        binding.btnRetry.setOnClickListener(v -> viewModel.search());
    }

    private void updateClearFiltersVisibility() {
        binding.btnClearFilters.setVisibility(
                viewModel.hasActiveFilters() ? View.VISIBLE : View.GONE
        );
    }

    private void showLoading() {
        binding.pbLoading.setVisibility(View.VISIBLE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.pbLoading.setVisibility(View.GONE);
    }

    private void showResults() {
        binding.rvSearchResults.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.btnLoadMore.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
        binding.btnLoadMore.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
