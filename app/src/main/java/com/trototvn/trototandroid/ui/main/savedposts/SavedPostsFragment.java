package com.trototvn.trototandroid.ui.main.savedposts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentSavedPostsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup back button
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Setup RecyclerView
        setupRecyclerView();

        // Load mock data
        loadMockData();
    }

    private void setupRecyclerView() {
        adapter = new ViewHistoryPostAdapter((post, position) -> {
            // Show confirmation dialog to remove from saved posts
            showRemoveConfirmationDialog(post, position);
        });

        binding.rvSavedPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSavedPosts.setAdapter(adapter);
    }

    private void showRemoveConfirmationDialog(PostItem post, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xoá khỏi bài đã lưu")
                .setMessage("Bạn có chắc chắn muốn xoá \"" + post.getTitle() + "\" khỏi bài đã lưu?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    // Remove from list
                    List<PostItem> currentList = new ArrayList<>(adapter.getCurrentList());
                    currentList.remove(position);
                    adapter.submitList(currentList);

                    Timber.d("Removed from saved - Post: %s", post.getTitle());
                    // TODO: Call API to unsave post

                    // Show empty state if list is now empty
                    if (currentList.isEmpty()) {
                        binding.rvSavedPosts.setVisibility(View.GONE);
                        binding.emptyState.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void loadMockData() {
        List<PostItem> savedPosts = new ArrayList<>();

        // Mock data - only saved posts (isSaved=true)
        savedPosts.add(new PostItem(2, "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=300&h=300&fit=crop", "Căn hộ 2 phòng ngủ view Bitexco",
                12.0, 65, "Trần Thị B", "", "TP. Hồ Chí Minh", true));

        savedPosts.add(new PostItem(6, "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=300&h=300&fit=crop", "Studio khu vực Landmark 81",
                8.0, 45, "Đỗ Thị F", "", "TP. Hồ Chí Minh", true));

        savedPosts.add(new PostItem(9, "https://images.unsplash.com/photo-1516214104703-d870798883c5?w=300&h=300&fit=crop", "Phòng tiện nghi gần ga Bình Thái",
                3.8, 22, "Đinh Văn I", "", "Thành phố Thủ Dầu Một", true));

        savedPosts.add(new PostItem(12, "https://images.unsplash.com/photo-1494145904049-0dca59b4bbad?w=300&h=300&fit=crop", "Studio gác lửng Quận 1",
                5.5, 28, "Linh Thị L", "", "TP. Hồ Chí Minh", true));

        savedPosts.add(new PostItem(15, "https://images.unsplash.com/photo-1525597099696-06628a4cb8e5?w=300&h=300&fit=crop", "Chung cư cao cấp khu Thảo Điền",
                14.0, 80, "Phúc Văn O", "", "TP. Hồ Chí Minh", true));

        // Show empty state if no saved posts
        if (savedPosts.isEmpty()) {
            binding.rvSavedPosts.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.VISIBLE);
        } else {
            binding.rvSavedPosts.setVisibility(View.VISIBLE);
            binding.emptyState.setVisibility(View.GONE);
            adapter.submitList(savedPosts);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

