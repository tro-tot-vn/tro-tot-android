package com.trototvn.trototandroid.ui.main.savedposts;

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

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.databinding.FragmentSavedPostsBinding;
import com.trototvn.trototandroid.ui.main.viewhistory.PostItem;
import com.trototvn.trototandroid.ui.main.viewhistory.ViewHistoryPostAdapter;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

/**
 * SavedPostsFragment - Shows user's saved posts
 */
@AndroidEntryPoint
public class SavedPostsFragment extends Fragment {

    private FragmentSavedPostsBinding binding;
    private ViewHistoryPostAdapter adapter;
    private SavedPostsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentSavedPostsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SavedPostsViewModel.class);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        setupRecyclerView();
        setupObservers();

        // Load saved posts from API
        viewModel.fetchSavedPosts();
    }

    private void setupRecyclerView() {
        adapter = new ViewHistoryPostAdapter((post, position) -> {
            // Navigate to post detail
            navigateToPostDetail(post.getId());
        });

        binding.rvSavedPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSavedPosts.setAdapter(adapter);
    }

    private void navigateToPostDetail(int postId) {
        // Implement navigation logic here
    }

    private void setupObservers() {
        viewModel.getSavedPostsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                if (resource.getData().isEmpty()) {
                    binding.rvSavedPosts.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.rvSavedPosts.setVisibility(View.VISIBLE);
                    binding.emptyState.setVisibility(View.GONE);
                    adapter.submitList(convertPostsToPostItems(resource.getData()));
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Timber.e("Error loading saved posts: %s", resource.getMessage());
                binding.rvSavedPosts.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getUnsaveStatusLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS) {
                // Refresh list after unsaving
                viewModel.fetchSavedPosts();
            }
        });
    }

    private List<PostItem> convertPostsToPostItems(List<Post> posts) {
        List<PostItem> postItems = new ArrayList<>();

        for (Post post : posts) {
            String imageUrl = getGoogleDriveImageUrl(post);
            
            // Lấy địa chỉ đầy đủ từ ward, district, city
            String fullLocation = (post.getWard() != null ? post.getWard() + ", " : "") +
                    (post.getDistrict() != null ? post.getDistrict() + ", " : "") +
                    (post.getCity() != null ? post.getCity() : "");

            PostItem item = new PostItem(
                    post.getPostId(),
                    imageUrl,
                    post.getTitle(),
                    post.getPrice(),
                    (int) post.getAcreage(),
                    fullLocation
            );
            postItems.add(item);
        }

        return postItems;
    }

    private String getGoogleDriveImageUrl(Post post) {
        if (post.getMultimediaFiles() != null && !post.getMultimediaFiles().isEmpty()) {
            com.trototvn.trototandroid.data.model.post.MultimediaFile file = post.getMultimediaFiles().get(0);
            if (file != null && file.getFile() != null) {
                String fileCloudId = file.getFile().getFileCloudId();
                if (fileCloudId != null && !fileCloudId.isEmpty()) {
                    return "https://drive.google.com/uc?id=" + fileCloudId + "&export=download";
                }
            }
        }
        return "";
    }

    private void showRemoveConfirmationDialog(PostItem post, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xoá khỏi bài đã lưu")
                .setMessage("Bạn có chắc chắn muốn xoá \"" + post.getTitle() + "\" khỏi bài đã lưu?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    viewModel.unsavePost(post.getId());
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
