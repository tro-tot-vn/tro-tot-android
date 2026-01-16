package com.trototvn.trototandroid.ui.postdetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trototvn.trototandroid.databinding.ItemPostImageBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for Post Detail image gallery
 * Uses PhotoView for zoom capability
 */
public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ImageViewHolder> {

    private List<String> imageUrls = new ArrayList<>();

    public void setImages(List<String> urls) {
        this.imageUrls = urls != null ? urls : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostImageBinding binding = ItemPostImageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(imageUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostImageBinding binding;

        public ImageViewHolder(ItemPostImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String imageUrl) {
            Glide.with(binding.photoView.getContext())
                    .load(imageUrl)
                    .centerInside()
                    .into(binding.photoView);
        }
    }
}
