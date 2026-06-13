package com.trototvn.trototandroid.ui.admin.review;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.model.admin.AdminPost;
import com.trototvn.trototandroid.databinding.ItemAdminPendingPostBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Adapter for the pending-post review queue.
 */
public class AdminPendingPostAdapter extends ListAdapter<AdminPost, AdminPendingPostAdapter.ViewHolder> {

    public interface OnPostClickListener {
        void onClick(AdminPost post);
    }

    private final OnPostClickListener listener;

    public AdminPendingPostAdapter(OnPostClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminPendingPostBinding binding = ItemAdminPendingPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminPendingPostBinding binding;
        private final OnPostClickListener listener;

        ViewHolder(ItemAdminPendingPostBinding binding, OnPostClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(AdminPost post) {
            binding.tvTitle.setText(post.getTitle());

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.tvPrice.setText(formatter.format(post.getPrice()) + "/tháng");

            String location = post.getShortLocation();
            String acreage = post.getAcreage() > 0 ? " · " + (int) post.getAcreage() + " m²" : "";
            binding.tvLocation.setText(location + acreage);

            String owner = post.getOwner() != null ? post.getOwner().getFullName() : "";
            binding.tvOwner.setText("Chủ tin: " + owner);

            binding.tvDate.setText(formatDate(post.getCreatedAt()));

            String imageUrl = firstImageUrl(post);
            if (imageUrl != null) {
                Glide.with(binding.ivThumbnail.getContext())
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(R.color.md_theme_light_surfaceVariant)
                        .error(R.color.md_theme_light_surfaceVariant)
                        .into(binding.ivThumbnail);
            } else {
                binding.ivThumbnail.setImageResource(R.color.md_theme_light_surfaceVariant);
            }

            binding.getRoot().setOnClickListener(v -> listener.onClick(post));
        }

        private static String firstImageUrl(AdminPost post) {
            if (post.getMultimediaFiles() == null) return null;
            for (AdminPost.MultimediaFile f : post.getMultimediaFiles()) {
                if (!f.isVideo()) {
                    return f.getUrl();
                }
            }
            return null;
        }

        private static String formatDate(String dateStr) {
            if (dateStr == null) return "";
            try {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                iso.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                Date date = iso.parse(dateStr.length() > 19 ? dateStr.substring(0, 19) : dateStr);
                if (date != null) {
                    SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return "Ngày đăng: " + out.format(date);
                }
            } catch (Exception ignored) {
            }
            return "";
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<AdminPost> {
        @Override
        public boolean areItemsTheSame(@NonNull AdminPost oldItem, @NonNull AdminPost newItem) {
            return oldItem.getPostId() == newItem.getPostId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AdminPost oldItem, @NonNull AdminPost newItem) {
            return oldItem.getPostId() == newItem.getPostId()
                    && oldItem.getStatus() != null
                    && oldItem.getStatus().equals(newItem.getStatus());
        }
    }
}
