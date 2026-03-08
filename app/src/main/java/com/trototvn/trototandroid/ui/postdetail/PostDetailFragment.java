package com.trototvn.trototandroid.ui.postdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.PostDetail;
import com.trototvn.trototandroid.databinding.FragmentPostDetailBinding;

import java.text.NumberFormat;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

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
        Timber.d("PostDetailFragment onViewCreated for postId: %d", postId);
        if (postId != -1) {
            viewModel.loadPostDetail(postId);
        } else {
            Timber.e("PostDetailFragment called with invalid postId: -1");
            Toast.makeText(requireContext(), "ID bài đăng không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupImageGallery() {
        imageAdapter = new PostImageAdapter();
        binding.vpImages.setAdapter(imageAdapter);
        binding.dotsIndicator.attachTo(binding.vpImages);
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
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPostDetail(PostDetail post) {
        // Images
        imageAdapter.setImages(post.getImageUrls());

        // Title
        binding.tvTitle.setText(post.getTitle());

        // Price
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
        String priceText = formatter.format(post.getPrice()) + "/tháng";
        binding.tvPrice.setText(priceText);

        // Phone (initially masked)
        String phone = post.getOwner().getAccount().getPhone();
        binding.tvPhone.setText(viewModel.getMaskedPhone(phone));

        binding.btnShowPhone.setOnClickListener(v -> {
            binding.tvPhone.setText(phone);
            binding.btnShowPhone.setVisibility(View.GONE);
        });

        // Description
        binding.tvDescription.setText(post.getDescription());
        binding.btnToggleDescription.setOnClickListener(v -> {
            if (binding.tvDescription.getMaxLines() == 3) {
                binding.tvDescription.setMaxLines(Integer.MAX_VALUE);
                binding.btnToggleDescription.setText(R.string.see_less);
            } else {
                binding.tvDescription.setMaxLines(3);
                binding.btnToggleDescription.setText(R.string.see_more);
            }
        });

        // Property Details Table
        binding.tableDetails.removeAllViews();
        addDetailRow(getString(R.string.acreage), String.format(Locale.getDefault(), "%.1f m²", post.getAcreage()));
        addDetailRow(getString(R.string.interior), post.getInteriorCondition());
        addDetailRow(getString(R.string.address), post.getFullAddress());
    }

    private void addDetailRow(String label, String value) {
        if (value == null || value.isEmpty())
            return;

        View rowView = LayoutInflater.from(requireContext()).inflate(R.layout.item_post_detail_row,
                binding.tableDetails, false);
        TextView tvLabel = rowView.findViewById(R.id.tvLabel);
        TextView tvValue = rowView.findViewById(R.id.tvValue);

        tvLabel.setText(label);
        tvValue.setText(value);

        binding.tableDetails.addView(rowView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
