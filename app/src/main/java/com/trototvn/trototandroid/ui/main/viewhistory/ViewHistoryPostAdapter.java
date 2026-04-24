package com.trototvn.trototandroid.ui.main.viewhistory;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.graphics.Color;
import android.graphics.PorterDuff;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.databinding.HistoryPostItemBinding;

/**
 * Adapter for list of posts (View History, Saved Posts, etc.)
 */
public class ViewHistoryPostAdapter extends ListAdapter<PostItem, ViewHistoryPostAdapter.ViewHolder> {

    public interface OnSaveClickListener {
        void onSaveClick(PostItem post, int position);
    }

    private final OnSaveClickListener saveClickListener;

    public ViewHistoryPostAdapter(OnSaveClickListener saveClickListener) {
        super(new DiffUtil.ItemCallback<PostItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull PostItem oldItem, @NonNull PostItem newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull PostItem oldItem, @NonNull PostItem newItem) {
                return oldItem.getTitle().equals(newItem.getTitle())
                        && oldItem.isSaved() == newItem.isSaved();
            }
        });
        this.saveClickListener = saveClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HistoryPostItemBinding binding = HistoryPostItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostItem post = getItem(position);
        holder.bind(post, position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final HistoryPostItemBinding binding;

        public ViewHolder(HistoryPostItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PostItem post, int position) {
            // Bind title
            binding.tvTitle.setText(post.getTitle());

            // Bind price and area
            binding.tvPrice.setText(String.format("%.1fM/tháng", post.getPrice()));
            binding.tvArea.setText(post.getArea() + "m²");

            // Bind location
            binding.tvLocation.setText(post.getLocation());

            // Bind poster info
            binding.tvPosterName.setText(post.getPosterName());

            // Load thumbnail with Glide
            Glide.with(binding.ivThumbnail.getContext())
                    .load(post.getThumbnail())
                    .centerCrop()
                    .into(binding.ivThumbnail);

            // Load poster avatar with Glide (with fallback to default avatar)
            String avatarUrl = post.getPosterAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(binding.ivPosterAvatar.getContext())
                        .load(avatarUrl)
                        .circleCrop()
                        .into(binding.ivPosterAvatar);
            } else {
                Glide.with(binding.ivPosterAvatar.getContext())
                        .load(R.drawable.ic_default_avatar)
                        .circleCrop()
                        .into(binding.ivPosterAvatar);
            }

            // Set save button state
            updateSaveButton(post);

            // Handle save button click
            binding.btnSave.setOnClickListener(v -> {
                post.setSaved(!post.isSaved());
                updateSaveButton(post);
                if (saveClickListener != null) {
                    saveClickListener.onSaveClick(post, position);
                }
            });
        }

        private void updateSaveButton(PostItem post) {
            binding.btnSave.setImageResource(R.drawable.ic_bookmark);
            if (post.isSaved()) {
                binding.btnSave.setColorFilter(Color.parseColor("#FF6B35"), PorterDuff.Mode.SRC_IN); // Cam khi lưu
            } else {
                binding.btnSave.setColorFilter(Color.parseColor("#CCCCCC"), PorterDuff.Mode.SRC_IN); // Xám
            }
        }
    }
}


