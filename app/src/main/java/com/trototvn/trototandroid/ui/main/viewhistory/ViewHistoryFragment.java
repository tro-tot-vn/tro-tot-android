package com.trototvn.trototandroid.ui.main.viewhistory;

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
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.databinding.FragmentViewHistoryBinding;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewHistoryFragment - Shows user's view history
 */
@AndroidEntryPoint
public class ViewHistoryFragment extends Fragment {

    private FragmentViewHistoryBinding binding;
    private ViewHistoryPostAdapter adapter;
    private ViewHistoryViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentViewHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ViewHistoryViewModel.class);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        setupRecyclerView();
        setupObservers();

        // Load view history from API
        viewModel.loadViewHistory();
    }

    private void setupRecyclerView() {
        adapter = new ViewHistoryPostAdapter((post, position) -> {
            Bundle bundle = new Bundle();
            bundle.putInt("postId", post.getId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.postDetailFragment, bundle);
        });

        binding.rvViewHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvViewHistory.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getViewHistory().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }

            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                adapter.submitList(convertPostsToPostItems(resource.getData()));
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Timber.e("Error loading view history: %s", resource.getMessage());
            }
        });
    }

    private List<PostItem> convertPostsToPostItems(List<Post> posts) {
        List<PostItem> postItems = new ArrayList<>();

        for (Post post : posts) {
            String imageUrl = getGoogleDriveImageUrl(post);
            
            // Lấy địa chỉ đầy đủ từ ward, district, city
            String fullLocation = post.getWard() + ", " + post.getDistrict() + ", " + post.getCity();

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

    private String
    getGoogleDriveImageUrl(Post post) {
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

