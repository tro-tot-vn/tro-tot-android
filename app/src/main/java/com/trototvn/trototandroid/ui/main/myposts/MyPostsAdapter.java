package com.trototvn.trototandroid.ui.main.myposts;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.post.ModerationHistory;
import com.trototvn.trototandroid.data.model.post.MyPost;
import com.trototvn.trototandroid.databinding.ItemMyPostBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import timber.log.Timber;

/**
 * Adapter for user's own posts listing
 * Uses ListAdapter and DiffUtil for optimized updates and animations
 */
public class MyPostsAdapter extends ListAdapter<MyPost, MyPostsAdapter.ViewHolder> {

    public interface OnPostActionListener {
        void onViewDetail(MyPost post);
        void onToggleHide(MyPost post);
        void onEditPost(MyPost post);
    }

    private final OnPostActionListener actionListener;

    public MyPostsAdapter(OnPostActionListener actionListener) {
        super(new DiffCallback());
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyPostBinding binding = ItemMyPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, actionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyPostBinding binding;
        private final OnPostActionListener actionListener;

        public ViewHolder(ItemMyPostBinding binding, OnPostActionListener actionListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.actionListener = actionListener;
        }

        public void bind(MyPost post) {
            // Title
            binding.tvTitle.setText(post.getTitle());

            // Price formatted in VND
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String priceText = formatter.format(post.getPrice()) + "/tháng";
            binding.tvPrice.setText(priceText);

            // Location
            String location = post.getDistrict() + ", " + post.getCity();
            binding.tvLocation.setText(location);

            // Formatted Date
            binding.tvDate.setText(formatDate(post.getCreatedAt()));

            // Image Thumbnail loading
            String imageUrl = post.getFirstImageUrl();
            if (imageUrl != null) {
                Glide.with(binding.ivPostImage.getContext())
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(R.color.md_theme_light_surfaceVariant)
                        .error(R.color.md_theme_light_surfaceVariant)
                        .into(binding.ivPostImage);
            } else {
                binding.ivPostImage.setImageResource(R.color.md_theme_light_surfaceVariant);
            }

            // Bind Dynamic Status Badge & Color Palettes
            bindStatusBadge(post.getStatus());

            // Bind Rejection Warning Banner with reason
            bindRejectionReason(post);

            // Action Buttons
            binding.btnViewDetail.setOnClickListener(v -> actionListener.onViewDetail(post));

            // Hide/Unhide Dynamic Button Configuration
            boolean isHidden = "HIDDEN".equals(post.getStatus());
            binding.btnToggleHide.setText(isHidden ? "Hiện tin đăng" : "Ẩn tin đăng");
            
            // Set simple dark filled button or outline for better UX hierarchy
            if (isHidden) {
                binding.btnToggleHide.setTextColor(Color.WHITE);
                binding.btnToggleHide.setBackgroundColor(Color.BLACK);
                binding.btnToggleHide.setStrokeWidth(0);
            } else {
                binding.btnToggleHide.setTextColor(Color.BLACK);
                binding.btnToggleHide.setBackgroundColor(Color.TRANSPARENT);
                binding.btnToggleHide.setStrokeWidth(1);
            }

            binding.btnToggleHide.setOnClickListener(v -> actionListener.onToggleHide(post));
            binding.btnEditPost.setOnClickListener(v -> actionListener.onEditPost(post));
        }

        private void bindStatusBadge(String status) {
            if (status == null) status = "";
            String badgeText;
            String textColorHex;
            String bgColorHex;

            switch (status.toUpperCase()) {
                case "APPROVED":
                case "EXPOSED":
                    badgeText = "ĐANG HIỂN THỊ";
                    textColorHex = "#17BF63"; // X Green
                    bgColorHex = "#1A17BF63"; // 10% Opacity
                    break;
                case "PENDING":
                    badgeText = "CHỜ DUYỆT";
                    textColorHex = "#F5A623"; // X Amber
                    bgColorHex = "#1AF5A623"; // 10% Opacity
                    break;
                case "REJECTED":
                    badgeText = "BỊ TỪ CHỐI";
                    textColorHex = "#FF3B30"; // Soft Red
                    bgColorHex = "#1AFF3B30"; // 10% Opacity
                    break;
                case "HIDDEN":
                    badgeText = "ĐÃ ẨN";
                    textColorHex = "#657786"; // Slate Grey
                    bgColorHex = "#1A657786"; // 10% Opacity
                    break;
                default:
                    badgeText = status.toUpperCase();
                    textColorHex = "#0F1419"; // Monochrome Text
                    bgColorHex = "#1A0F1419"; // 10% Opacity
                    break;
            }

            binding.tvStatus.setText(badgeText);
            binding.tvStatus.setTextColor(Color.parseColor(textColorHex));
            binding.cardStatus.setCardBackgroundColor(Color.parseColor(bgColorHex));
        }

        private void bindRejectionReason(MyPost post) {
            if ("REJECTED".equalsIgnoreCase(post.getStatus())) {
                List<ModerationHistory> histories = post.getModerationHistories();
                if (histories != null && !histories.isEmpty()) {
                    // Extract the latest moderation action reason
                    String reason = histories.get(histories.size() - 1).getReason();
                    if (reason != null && !reason.trim().isEmpty()) {
                        binding.tvRejectReason.setText("Lý do từ chối: " + reason);
                        binding.cardRejectReason.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            }
            binding.cardRejectReason.setVisibility(View.GONE);
        }

        private String formatDate(String dateStr) {
            if (dateStr == null) return "Đăng ngày: --/--/----";
            try {
                // Parse ISO-8601 Format (Standard UTC returned by Backend)
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = isoFormat.parse(dateStr);
                if (date != null) {
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return "Đăng ngày: " + outputFormat.format(date);
                }
            } catch (Exception e) {
                Timber.e(e, "Error parsing ISO date: %s", dateStr);
            }
            return "Đăng ngày: " + dateStr;
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<MyPost> {
        @Override
        public boolean areItemsTheSame(@NonNull MyPost oldItem, @NonNull MyPost newItem) {
            return oldItem.getPostId() == newItem.getPostId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MyPost oldItem, @NonNull MyPost newItem) {
            return oldItem.getStatus().equals(newItem.getStatus())
                    && oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getPrice() == newItem.getPrice();
        }
    }
}
