package com.trototvn.trototandroid.ui.admin.review;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.trototvn.trototandroid.data.model.admin.PostModerationHistoryItem;
import com.trototvn.trototandroid.databinding.ItemAdminHistoryBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Adapter for a post's moderation history.
 */
public class AdminHistoryAdapter extends ListAdapter<PostModerationHistoryItem, AdminHistoryAdapter.ViewHolder> {

    public AdminHistoryAdapter() {
        super(new DiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminHistoryBinding binding = ItemAdminHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminHistoryBinding binding;

        ViewHolder(ItemAdminHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PostModerationHistoryItem item) {
            boolean approved = "Approved".equalsIgnoreCase(item.getActionType());
            binding.tvAction.setText(approved ? "Đã duyệt" : "Đã từ chối");
            binding.tvAction.setTextColor(Color.parseColor(approved ? "#17BF63" : "#FF3B30"));

            String admin = item.getAdmin() != null ? item.getAdmin().getFullName() : "";
            binding.tvMeta.setText(formatDate(item.getExecAt()) + " · " + admin);

            if (item.getReason() != null && !item.getReason().trim().isEmpty()) {
                binding.tvReason.setText("Lý do: " + item.getReason());
                binding.tvReason.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvReason.setVisibility(android.view.View.GONE);
            }
        }

        private static String formatDate(String dateStr) {
            if (dateStr == null) return "";
            try {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                iso.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                Date date = iso.parse(dateStr.length() > 19 ? dateStr.substring(0, 19) : dateStr);
                if (date != null) {
                    SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    return out.format(date);
                }
            } catch (Exception ignored) {
            }
            return dateStr;
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<PostModerationHistoryItem> {
        @Override
        public boolean areItemsTheSame(@NonNull PostModerationHistoryItem o, @NonNull PostModerationHistoryItem n) {
            return o.getExecAt() != null && o.getExecAt().equals(n.getExecAt())
                    && o.getActionType() != null && o.getActionType().equals(n.getActionType());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PostModerationHistoryItem o, @NonNull PostModerationHistoryItem n) {
            return areItemsTheSame(o, n);
        }
    }
}
