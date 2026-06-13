package com.trototvn.trototandroid.ui.admin.review;

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

import com.google.gson.Gson;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.AdminPost;
import com.trototvn.trototandroid.databinding.FragmentAdminReviewBinding;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Pending-post review queue. Backend: GET api/admin/posts/pending.
 * Row click opens the moderation detail, passing the post as a Gson JSON string
 * (there is no get-pending-by-id endpoint).
 */
@AndroidEntryPoint
public class AdminReviewFragment extends Fragment {

    private FragmentAdminReviewBinding binding;
    private AdminReviewViewModel viewModel;
    private AdminPendingPostAdapter adapter;
    private final Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminReviewViewModel.class);

        adapter = new AdminPendingPostAdapter(this::openDetail);
        binding.rvPendingPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPendingPosts.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadPendingPosts());

        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), this::render);
        // Initial load + refresh-on-return handled by onResume (runs after onViewCreated)
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadPendingPosts();
        }
    }

    private void openDetail(AdminPost post) {
        Bundle args = new Bundle();
        args.putInt("postId", post.getPostId());
        args.putString("postJson", gson.toJson(post));
        NavHostFragment.findNavController(this).navigate(R.id.action_review_to_detail, args);
    }

    private void render(Resource<List<AdminPost>> resource) {
        if (resource == null) return;
        switch (resource.getStatus()) {
            case LOADING:
                if (!binding.swipeRefresh.isRefreshing()) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
                binding.tvEmpty.setVisibility(View.GONE);
                break;
            case SUCCESS:
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                List<AdminPost> posts = resource.getData();
                if (posts == null || posts.isEmpty()) {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    adapter.submitList(java.util.Collections.emptyList());
                } else {
                    binding.tvEmpty.setVisibility(View.GONE);
                    adapter.submitList(posts);
                }
                break;
            case ERROR:
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
