package com.trototvn.trototandroid.ui.postdetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trototvn.trototandroid.data.model.rating.Rating;
import com.trototvn.trototandroid.databinding.ItemRatingBinding;
import com.trototvn.trototandroid.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostRatingAdapter extends RecyclerView.Adapter<PostRatingAdapter.RatingViewHolder> {

    private List<Rating> ratings = new ArrayList<>();

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings != null ? ratings : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRatingBinding binding = ItemRatingBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RatingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        holder.bind(ratings.get(position));
    }

    @Override
    public int getItemCount() {
        return ratings.size();
    }

    static class RatingViewHolder extends RecyclerView.ViewHolder {
        private final ItemRatingBinding binding;

        public RatingViewHolder(ItemRatingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Rating rating) {
            // Set name
            String fullName = "Ẩn danh";
            String initials = "AD";
            if (rating.getRater() != null) {
                fullName = rating.getRater().getFullName();
                
                String fn = rating.getRater().getFirstName();
                String ln = rating.getRater().getLastName();
                String tempInitials = "";
                if (ln != null && !ln.isEmpty()) {
                    tempInitials += ln.substring(0, 1).toUpperCase();
                }
                if (fn != null && !fn.isEmpty()) {
                    tempInitials += fn.substring(0, 1).toUpperCase();
                }
                if (!tempInitials.isEmpty()) {
                    initials = tempInitials;
                } else if (!fullName.isEmpty()) {
                    initials = fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
                }
            }
            binding.tvRaterName.setText(fullName);
            binding.tvAvatarInitials.setText(initials);

            // Set Rating stars
            binding.ratingBar.setRating(rating.getNumRate());

            // Set Date
            binding.tvRatingDate.setText(getFormattedDate(rating.getCreatedAt()));

            // Set Comment
            if (rating.getComment() != null && !rating.getComment().trim().isEmpty()) {
                binding.tvRatingComment.setText(rating.getComment());
                binding.tvRatingComment.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvRatingComment.setVisibility(android.view.View.GONE);
            }
        }

        private String getFormattedDate(String isoDate) {
            if (isoDate == null || isoDate.isEmpty()) return "";
            try {
                String cleanDate = isoDate;
                if (cleanDate.contains(".")) {
                    cleanDate = cleanDate.substring(0, cleanDate.indexOf("."));
                }
                if (cleanDate.contains("Z")) {
                    cleanDate = cleanDate.replace("Z", "");
                }
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                java.util.Date parsed = parser.parse(cleanDate);
                if (parsed != null) {
                    return DateUtils.getRelativeTime(parsed);
                }
            } catch (Exception e) {
                try {
                    if (isoDate.length() >= 10) {
                        return isoDate.substring(0, 10);
                    }
                } catch (Exception ignored) {}
            }
            return isoDate;
        }
    }
}
