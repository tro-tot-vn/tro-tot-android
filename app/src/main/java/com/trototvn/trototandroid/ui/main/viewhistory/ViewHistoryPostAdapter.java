package com.trototvn.trototandroid.ui.main.viewhistory;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trototvn.trototandroid.databinding.HistoryPostItemBinding;

/**
 * Adapter for list of posts (View History, Saved Posts, etc.)
 */
public class ViewHistoryPostAdapter extends ListAdapter<PostItem, ViewHistoryPostAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PostItem post, int position);
    }

    private final OnItemClickListener itemClickListener;

    public ViewHistoryPostAdapter(OnItemClickListener itemClickListener) {
        super(new DiffUtil.ItemCallback<PostItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull PostItem oldItem, @NonNull PostItem newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull PostItem oldItem, @NonNull PostItem newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }
        });
        this.itemClickListener = itemClickListener;
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
            double priceInMillions = post.getPrice() / 1000000.0;
            if (priceInMillions >= 1) {
                binding.tvPrice.setText(String.format("%.1f triệu/tháng", priceInMillions));
            } else {
                binding.tvPrice.setText(String.format("%,.0f đ/tháng", post.getPrice()));
            }
            binding.tvArea.setText(post.getArea() + "m²");

            // Bind location
            binding.tvLocation.setText(post.getLocation());

            // Load thumbnail with Glide
            Glide.with(binding.ivThumbnail.getContext())
                    .load(post.getThumbnail())
                    .centerCrop()
                    .into(binding.ivThumbnail);

            // Handle root click (Navigate to detail)
            binding.getRoot().setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(post, position);
                }
            });
        }
    }
}
