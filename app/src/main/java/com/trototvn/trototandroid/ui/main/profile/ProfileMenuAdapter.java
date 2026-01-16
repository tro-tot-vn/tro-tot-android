package com.trototvn.trototandroid.ui.main.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.trototvn.trototandroid.databinding.ItemProfileMenuBinding;

/**
 * Adapter for profile menu items
 */
public class ProfileMenuAdapter extends ListAdapter<ProfileMenuItem, ProfileMenuAdapter.ViewHolder> {

    private final OnMenuItemClickListener listener;

    public interface OnMenuItemClickListener {
        void onMenuItemClick(ProfileMenuItem item);
    }

    public ProfileMenuAdapter(OnMenuItemClickListener listener) {
        super(new DiffUtil.ItemCallback<ProfileMenuItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ProfileMenuItem oldItem, @NonNull ProfileMenuItem newItem) {
                return oldItem.getType() == newItem.getType();
            }

            @Override
            public boolean areContentsTheSame(@NonNull ProfileMenuItem oldItem, @NonNull ProfileMenuItem newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                       (oldItem.getSubtitle() == null ? newItem.getSubtitle() == null : 
                        oldItem.getSubtitle().equals(newItem.getSubtitle()));
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProfileMenuBinding binding = ItemProfileMenuBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProfileMenuBinding binding;

        public ViewHolder(ItemProfileMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ProfileMenuItem item, OnMenuItemClickListener listener) {
            binding.ivIcon.setImageResource(item.getIconRes());
            binding.tvTitle.setText(item.getTitle());
            
            if (item.getSubtitle() != null && !item.getSubtitle().isEmpty()) {
                binding.tvSubtitle.setText(item.getSubtitle());
                binding.tvSubtitle.setVisibility(View.VISIBLE);
            } else {
                binding.tvSubtitle.setVisibility(View.GONE);
            }

            binding.layoutMenuItem.setOnClickListener(v -> listener.onMenuItemClick(item));
        }
    }
}
