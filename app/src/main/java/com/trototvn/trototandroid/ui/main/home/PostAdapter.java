package com.trototvn.trototandroid.ui.main.home;

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
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.databinding.ItemPostCardBinding;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * RecyclerView Adapter for displaying Post items
 * Uses DiffUtil for efficient updates
 */
public class PostAdapter extends ListAdapter<Post, PostAdapter.PostViewHolder> {

    private final OnPostClickListener clickListener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public PostAdapter(OnPostClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {
        @Override
        public boolean areItemsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.getPostId() == newItem.getPostId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getPrice() == newItem.getPrice() &&
                    oldItem.getCity().equals(newItem.getCity());
        }
    };

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostCardBinding binding = ItemPostCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener);
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostCardBinding binding;

        public PostViewHolder(ItemPostCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Post post, OnPostClickListener clickListener) {
            // Title
            binding.tvTitle.setText(post.getTitle());

            // Price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String priceText = formatter.format(post.getPrice()) + "/tháng";
            binding.tvPrice.setText(priceText);

            // Location
            String location = post.getDistrict() + ", " + post.getCity();
            binding.tvLocation.setText(location);

            // Acreage
            String acreage = String.format(Locale.getDefault(), "%.0f m²", post.getAcreage());
            binding.tvAcreage.setText(acreage);

            // Time ago
            binding.tvTimeAgo.setText(getTimeAgo(post.getCreatedAt().getTime()));

            // Image
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

            // Click listener
            itemView.setOnClickListener(v -> clickListener.onPostClick(post));
        }

        private String getTimeAgo(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days == 0) {
                return "Hôm nay";
            } else if (days <= 7) {
                return days + " ngày trước";
            } else {
                return "7+ ngày trước";
            }
        }
    }
}
