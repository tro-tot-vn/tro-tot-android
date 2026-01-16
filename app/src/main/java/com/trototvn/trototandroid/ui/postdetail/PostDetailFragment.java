package com.trototvn.trototandroid.ui.postdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.PostDetail;
import com.trototvn.trototandroid.databinding.FragmentPostDetailBinding;

import java.text.NumberFormat;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Post Detail Fragment - Session 1: Basic UI
 * Displays post information with image gallery
 */
@AndroidEntryPoint
public class PostDetailFragment extends Fragment {

    private FragmentPostDetailBinding binding;
    private PostDetailViewModel viewModel;
    private PostImageAdapter imageAdapter;
    private int postId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get postId from arguments
        if (getArguments() != null) {
            postId = getArguments().getInt("postId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        setupImageGallery();
        observeData();

        // Load post detail
        if (postId != -1) {
            viewModel.loadPostDetail(postId);
        }
    }

    private void setupImageGallery() {
        imageAdapter = new PostImageAdapter();
        binding.vpImages.setAdapter(imageAdapter);
        // TODO Session 2: Manually connect indicator to ViewPager2
        // binding.indicator.setViewPager(binding.vpImages);
    }

    private void observeData() {
        viewModel.getPostDetail().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.LOADING) {
                binding.pbLoading.setVisibility(View.VISIBLE);
            } else if (resource.getStatus() == Resource.Status.SUCCESS) {
                binding.pbLoading.setVisibility(View.GONE);
                if (resource.getData() != null) {
                    displayPostDetail(resource.getData());
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.pbLoading.setVisibility(View.GONE);
                // Show error
            }
        });
    }

    private void displayPostDetail(PostDetail post) {
        // Images
        imageAdapter.setImages(post.getImageUrls());

        // Title
        binding.tvTitle.setText(post.getTitle());

        // Price
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String priceText = formatter.format(post.getPrice()) + "/tháng";
        binding.tvPrice.setText(priceText);

        // Phone (masked)
        String phone = post.getOwner().getAccount().getPhone();
        binding.tvPhone.setText(viewModel.getMaskedPhone(phone));

        // Description
        binding.tvDescription.setText(post.getDescription());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
