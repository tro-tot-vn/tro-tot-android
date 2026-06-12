package com.trototvn.trototandroid.ui.admin.moderators;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.trototvn.trototandroid.data.model.admin.Moderator;
import com.trototvn.trototandroid.databinding.ItemAdminModeratorBinding;

/**
 * Adapter for the moderator list (Manager-only screen).
 */
public class ModeratorAdapter extends ListAdapter<Moderator, ModeratorAdapter.ViewHolder> {

    public interface OnModeratorActionListener {
        void onProfile(Moderator moderator);

        void onHistory(Moderator moderator);

        void onResetPassword(Moderator moderator);

        void onToggleStatus(Moderator moderator);
    }

    private final OnModeratorActionListener listener;

    public ModeratorAdapter(OnModeratorActionListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminModeratorBinding binding = ItemAdminModeratorBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminModeratorBinding binding;
        private final OnModeratorActionListener listener;

        ViewHolder(ItemAdminModeratorBinding binding, OnModeratorActionListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Moderator m) {
            binding.tvName.setText(m.getFullName());
            binding.tvEmail.setText(m.getAccount() != null ? m.getAccount().getEmail() : "");
            binding.tvPhone.setText(m.getAccount() != null ? m.getAccount().getPhone() : "");

            boolean active = m.isActive();
            binding.tvStatus.setText(active ? "Hoạt động" : "Tạm khóa");
            binding.tvStatus.setTextColor(Color.parseColor(active ? "#17BF63" : "#FF3B30"));
            binding.btnToggleStatus.setText(active ? "Khóa" : "Mở khóa");

            binding.btnProfile.setOnClickListener(v -> listener.onProfile(m));
            binding.btnHistory.setOnClickListener(v -> listener.onHistory(m));
            binding.btnReset.setOnClickListener(v -> listener.onResetPassword(m));
            binding.btnToggleStatus.setOnClickListener(v -> listener.onToggleStatus(m));
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<Moderator> {
        @Override
        public boolean areItemsTheSame(@NonNull Moderator o, @NonNull Moderator n) {
            return o.getAdminId() == n.getAdminId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Moderator o, @NonNull Moderator n) {
            return o.getAdminId() == n.getAdminId()
                    && o.getStatus() != null && o.getStatus().equals(n.getStatus());
        }
    }
}
