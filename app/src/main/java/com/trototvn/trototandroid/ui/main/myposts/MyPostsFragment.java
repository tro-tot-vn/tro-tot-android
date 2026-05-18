package com.trototvn.trototandroid.ui.main.myposts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.MyPost;
import com.trototvn.trototandroid.databinding.FragmentMypostsBinding;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * Fragment to display and manage user's own posts
 * Implements filtering, endless cursor-based pagination, and hide/unhide toggling with a modern X style
 */
@AndroidEntryPoint
public class MyPostsFragment extends Fragment {

    private FragmentMypostsBinding binding;
    private MyPostsViewModel viewModel;
    private MyPostsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMypostsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MyPostsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupFilters();
        setupObservers();

        // Initial Load
        viewModel.loadMyPosts(true);
    }

    private void setupToolbar() {
        binding.ivBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.tvHeaderSubtitle.setText("Đang tải...");
        
        binding.btnCreatePost.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.postCreateFragment);
        });
        binding.ivToolbarCreate.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.postCreateFragment);
        });
    }

    private void setupRecyclerView() {
        adapter = new MyPostsAdapter(new MyPostsAdapter.OnPostActionListener() {
            @Override
            public void onViewDetail(MyPost post) {
                Bundle bundle = new Bundle();
                bundle.putInt("postId", post.getPostId());
                NavHostFragment.findNavController(MyPostsFragment.this)
                        .navigate(R.id.postDetailFragment, bundle);
            }

            @Override
            public void onToggleHide(MyPost post) {
                viewModel.togglePostVisibility(post);
            }

            @Override
            public void onEditPost(MyPost post) {
                Bundle bundle = new Bundle();
                bundle.putInt("postId", post.getPostId());
                NavHostFragment.findNavController(MyPostsFragment.this)
                        .navigate(R.id.postEditFragment, bundle);
            }
        });

        binding.rvMyPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyPosts.setAdapter(adapter);

        // Swipe Refresh Layout
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadMyPosts(true));

        // Endless Cursor Pagination Scroll Listener
        binding.rvMyPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && viewModel.hasMore()) {
                        viewModel.loadMyPosts(false);
                    }
                }
            }
        });
    }

    private void setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            String status = null;

            if (checkedId == R.id.chipActive) {
                status = "Approved";
            } else if (checkedId == R.id.chipPending) {
                status = "Pending";
            } else if (checkedId == R.id.chipRejected) {
                status = "Rejected";
            } else if (checkedId == R.id.chipHidden) {
                status = "Hidden";
            }

            viewModel.setFilterStatus(status);
        });
    }

    private void setupObservers() {
        // Observe posts state
        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            boolean isRefresh = resource.getData() == null || binding.swipeRefreshLayout.isRefreshing();

            if (resource.getStatus() == Resource.Status.LOADING) {
                if (!binding.swipeRefreshLayout.isRefreshing()) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
                binding.layoutEmpty.setVisibility(View.GONE);
            } else if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);

                List<MyPost> posts = resource.getData();
                if (posts.isEmpty()) {
                    binding.rvMyPosts.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    binding.tvHeaderSubtitle.setText("0 bài đăng");
                } else {
                    binding.rvMyPosts.setVisibility(View.VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                    adapter.submitList(posts);
                    binding.tvHeaderSubtitle.setText(posts.size() + " bài đăng");
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                Timber.e("Error loading my posts: %s", resource.getMessage());
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();

                // If currently showing cached data, keep it, otherwise show empty
                if (resource.getData() == null || resource.getData().isEmpty()) {
                    binding.rvMyPosts.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    binding.tvHeaderSubtitle.setText("Lỗi tải tin");
                }
            }
        });

        // Observe toggle actions (Hide/Unhide post status)
        viewModel.getActionStateLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.getStatus() == Resource.Status.LOADING) {
                // Actions loading state handled with custom button overlay if desired, 
                // but let's show simple non-blocking indicator
            } else if (resource.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Lỗi: " + resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
