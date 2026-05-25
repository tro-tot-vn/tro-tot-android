package com.trototvn.trototandroid.ui.postdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;
import com.bumptech.glide.Glide;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.PostDetail;
import com.trototvn.trototandroid.data.model.post.MultimediaFile;
import com.trototvn.trototandroid.data.model.rating.Rating;
import com.trototvn.trototandroid.data.model.rating.RatingStats;
import com.trototvn.trototandroid.databinding.FragmentPostDetailBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * Post Detail Fragment
 * Displays post information, image gallery, and handles ratings/reviews
 */
@AndroidEntryPoint
public class PostDetailFragment extends Fragment {

    private FragmentPostDetailBinding binding;
    private PostDetailViewModel viewModel;
    private PostImageAdapter imageAdapter;
    private PostRatingAdapter ratingAdapter;
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
        setupRatingsList();
        setupRatingSubmission();

        binding.fabSave.setOnClickListener(v -> {
            if (postId != -1) {
                viewModel.toggleSavePost(postId);
            }
        });

        observeData();

        // Load post detail & rating info
        Timber.d("PostDetailFragment onViewCreated for postId: %d", postId);
        if (postId != -1) {
            viewModel.loadPostDetail(postId);
            viewModel.loadRatingStats(postId);
            viewModel.loadMyRating(postId);
            viewModel.loadRatings(postId);
            viewModel.checkIfSaved(postId);
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

    private void setupRatingsList() {
        ratingAdapter = new PostRatingAdapter();
        binding.rvRatings.setAdapter(ratingAdapter);
        binding.rvRatings.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
    }

    private void setupRatingSubmission() {
        binding.btnSubmitRating.setOnClickListener(v -> {
            int stars = (int) binding.ratingBarWrite.getRating();
            String comment = binding.etComment.getText() != null ? binding.etComment.getText().toString().trim() : "";
            if (stars < 1) {
                Toast.makeText(requireContext(), "Vui lòng chọn số sao từ 1 đến 5", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.submitRating(postId, stars, comment);
        });

        binding.btnDeleteRating.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xóa đánh giá")
                    .setMessage("Bạn có chắc chắn muốn xóa đánh giá này?")
                    .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteRating(postId))
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void observeData() {
        // Observe Post Detail
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

        // Observe Rating Stats
        viewModel.getRatingStats().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                RatingStats stats = resource.getData();
                binding.tvAvgRating.setText(String.format(Locale.getDefault(), "%.1f", stats.getAvgRate()));
                binding.ratingBarStats.setRating((float) stats.getAvgRate());
                if (stats.getCountRate() > 0) {
                    binding.tvRatingCount.setText(String.format(Locale.getDefault(), "(%d đánh giá)", stats.getCountRate()));
                } else {
                    binding.tvRatingCount.setText("Chưa có đánh giá");
                }
            } else {
                binding.tvAvgRating.setText("0.0");
                binding.ratingBarStats.setRating(0.0f);
                binding.tvRatingCount.setText("Chưa có đánh giá");
            }
        });

        // Observe Current User's Rating
        viewModel.getMyRating().observe(getViewLifecycleOwner(), resource -> {
            if (!viewModel.isAuthenticated()) {
                binding.tvLoginPrompt.setVisibility(View.VISIBLE);
                binding.layoutWriteRating.setVisibility(View.GONE);
                binding.layoutMyRatingSubmitted.setVisibility(View.GONE);
            } else if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                // User has already rated
                Rating rating = resource.getData();
                binding.tvLoginPrompt.setVisibility(View.GONE);
                binding.layoutWriteRating.setVisibility(View.GONE);
                binding.layoutMyRatingSubmitted.setVisibility(View.VISIBLE);

                binding.ratingBarMySubmitted.setRating(rating.getNumRate());
                if (rating.getComment() != null && !rating.getComment().trim().isEmpty()) {
                    binding.tvMyRatingComment.setText(rating.getComment());
                    binding.tvMyRatingComment.setVisibility(View.VISIBLE);
                } else {
                    binding.tvMyRatingComment.setVisibility(View.GONE);
                }
            } else if (resource.getStatus() == Resource.Status.LOADING) {
                // Loading, keep stable
            } else {
                // User has not rated yet
                binding.tvLoginPrompt.setVisibility(View.GONE);
                binding.layoutWriteRating.setVisibility(View.VISIBLE);
                binding.layoutMyRatingSubmitted.setVisibility(View.GONE);

                // Reset forms
                binding.ratingBarWrite.setRating(5.0f);
                binding.etComment.setText("");
            }
        });

        // Observe Reviews List
        viewModel.getRatingsList().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                List<Rating> list = resource.getData().getDataPag();
                if (list != null && !list.isEmpty()) {
                    binding.tvNoReviews.setVisibility(View.GONE);
                    binding.rvRatings.setVisibility(View.VISIBLE);
                    ratingAdapter.setRatings(list);
                } else {
                    binding.tvNoReviews.setVisibility(View.VISIBLE);
                    binding.rvRatings.setVisibility(View.GONE);
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.tvNoReviews.setText("Không thể tải nhận xét");
                binding.tvNoReviews.setVisibility(View.VISIBLE);
                binding.rvRatings.setVisibility(View.GONE);
            }
        });

        // Observe Saved Status
        viewModel.getIsSaved().observe(getViewLifecycleOwner(), isSaved -> {
            if (isSaved != null && isSaved) {
                binding.fabSave.setImageResource(R.drawable.ic_bookmark_filled);
            } else {
                binding.fabSave.setImageResource(R.drawable.ic_bookmark_border);
            }
        });

        // Observe Save/Unsave Action Status
        viewModel.getSaveStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                boolean isSaved = viewModel.getIsSaved().getValue() != null && viewModel.getIsSaved().getValue();
                Toast.makeText(requireContext(), isSaved ? "Đã lưu tin đăng" : "Đã bỏ lưu tin đăng", Toast.LENGTH_SHORT).show();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPostDetail(PostDetail post) {
        // Diagnostic Logging
        Timber.d("=== DIAGNOSTIC: displayPostDetail ===");
        if (post.getMultimediaFiles() != null) {
            Timber.d("Multimedia files count: %d", post.getMultimediaFiles().size());
            for (int i = 0; i < post.getMultimediaFiles().size(); i++) {
                MultimediaFile mf = post.getMultimediaFiles().get(i);
                if (mf != null) {
                    Timber.d("File[%d]: junction fileId=%d", i, mf.getFileId());
                    if (mf.getFile() != null) {
                        Timber.d("  -> fileDetail: fileId=%d, fileType=%s", 
                                 mf.getFile().getFileId(), 
                                 mf.getFile().getFileType() != null ? mf.getFile().getFileType().name() : "NULL");
                    } else {
                        Timber.d("  -> fileDetail is NULL!");
                    }
                }
            }
        } else {
            Timber.d("Multimedia files list is NULL!");
        }
        
        List<String> urls = post.getImageUrls();
        Timber.d("Final extracted image URLs count: %d", urls.size());
        for (String url : urls) {
            Timber.d("  -> URL: %s", url);
        }
        
        // Images
        imageAdapter.setImages(urls);

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

        // Google Maps WebView
        binding.wvMap.getSettings().setJavaScriptEnabled(true);
        binding.wvMap.setWebViewClient(new WebViewClient());
        String address = post.getFullAddress();
        String apiKey = "AIzaSyDDNu6RXhhzn6OpjxnD886FhA7owrg2yYk";
        String mapUrl = "https://www.google.com/maps/embed/v1/place?key=" + apiKey + "&q=" + Uri.encode(address);

        String html = "<!DOCTYPE html><html><body style='margin:0;padding:0;'>" +
                "<iframe width='100%' height='100%' frameborder='0' style='border:0; position:fixed; top:0; left:0; bottom:0; right:0;' " +
                "src='" + mapUrl + "' allowfullscreen></iframe>" +
                "</body></html>";
        binding.wvMap.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

        // Seller Information
        com.trototvn.trototandroid.data.model.post.PostOwner seller = post.getOwner();
        binding.tvSellerName.setText(seller.getFullName());

        StringBuilder location = new StringBuilder();
        if (seller.getCurrentDistrict() != null && !seller.getCurrentDistrict().isEmpty()) {
            location.append(seller.getCurrentDistrict());
        }
        if (seller.getCurrentCity() != null && !seller.getCurrentCity().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(seller.getCurrentCity());
        }
        if (location.length() == 0) {
            binding.tvSellerLocation.setText("Chưa có thông tin địa chỉ");
        } else {
            binding.tvSellerLocation.setText(location.toString());
        }

        if (seller.getJoinedAt() != null && !seller.getJoinedAt().isEmpty()) {
            try {
                String joined = seller.getJoinedAt();
                if (joined.contains("T")) {
                    joined = joined.substring(0, joined.indexOf("T"));
                }
                binding.tvSellerJoined.setText("Thành viên từ: " + joined);
            } catch (Exception e) {
                binding.tvSellerJoined.setText("Thành viên từ: " + seller.getJoinedAt());
            }
        } else {
            binding.tvSellerJoined.setText("Thành viên mới");
        }

        if (seller.getAvatar() != null && !seller.getAvatar().isEmpty()) {
            String avatarUrl = seller.getAvatar();
            if (!avatarUrl.startsWith("http")) {
                avatarUrl = com.trototvn.trototandroid.utils.Constants.BASE_URL + "api/files/" + avatarUrl;
            }
            Glide.with(this)
                 .load(avatarUrl)
                 .placeholder(R.drawable.ic_default_avatar)
                 .error(R.drawable.ic_default_avatar)
                 .into(binding.ivSellerAvatar);
        } else {
            binding.ivSellerAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        binding.btnSellerPhone.setOnClickListener(v -> {
            if (!viewModel.isAuthenticated()) {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem số điện thoại", Toast.LENGTH_LONG).show();
            } else {
                binding.btnSellerPhone.setText(phone);
            }
        });

        binding.btnSellerChat.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tính năng Chat sẽ sớm được ra mắt!", Toast.LENGTH_SHORT).show();
        });

        // Quick Messaging Setup
        binding.btnQuickMsg1.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tính năng gửi tin nhắn nhanh sẽ sớm được ra mắt!", Toast.LENGTH_SHORT).show();
        });
        binding.btnQuickMsg2.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tính năng gửi tin nhắn nhanh sẽ sớm được ra mắt!", Toast.LENGTH_SHORT).show();
        });
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
