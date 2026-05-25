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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.trototvn.trototandroid.R;
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
    private List<PostItem> currentItems = new ArrayList<>();

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
            Bundle bundle = new Bundle();
            bundle.putInt("postId", post.getId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.postDetailFragment, bundle);
        });

        binding.rvSavedPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSavedPosts.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PostItem deletedItem = currentItems.get(position);
                    
                    // Remove from local list
                    currentItems.remove(position);
                    adapter.submitList(new ArrayList<>(currentItems));
                    
                    if (currentItems.isEmpty()) {
                        binding.rvSavedPosts.setVisibility(View.GONE);
                        binding.emptyState.setVisibility(View.VISIBLE);
                    }

                    Snackbar.make(binding.getRoot(), "Đã xoá bài viết khỏi danh sách đã lưu", Snackbar.LENGTH_LONG)
                            .setAction("Hoàn tác", v -> {
                                // Undo action
                                currentItems.add(position, deletedItem);
                                adapter.submitList(new ArrayList<>(currentItems));
                                
                                binding.rvSavedPosts.setVisibility(View.VISIBLE);
                                binding.emptyState.setVisibility(View.GONE);
                            })
                            .addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    if (event != DISMISS_EVENT_ACTION) {
                                        // Execute actual delete on server
                                        viewModel.unsavePost(deletedItem.getId());
                                    }
                                }
                            }).show();
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.rvSavedPosts);
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
                    currentItems = convertPostsToPostItems(resource.getData());
                    adapter.submitList(new ArrayList<>(currentItems));
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Timber.e("Error loading saved posts: %s", resource.getMessage());
                binding.rvSavedPosts.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getUnsaveStatusLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS) {
                // Khong can reload cho mượt, vì đã xoá ở local 
                // viewModel.fetchSavedPosts();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
