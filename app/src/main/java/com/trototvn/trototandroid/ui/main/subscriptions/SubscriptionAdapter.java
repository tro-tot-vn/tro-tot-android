package com.trototvn.trototandroid.ui.main.subscriptions;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.trototvn.trototandroid.data.model.profile.Subscription;
import com.trototvn.trototandroid.databinding.ItemSubscriptionBinding;

public class SubscriptionAdapter extends ListAdapter<Subscription, SubscriptionAdapter.ViewHolder> {

    private final OnSubscriptionDeleteListener listener;

    public interface OnSubscriptionDeleteListener {
        void onDelete(Subscription subscription);
    }

    public SubscriptionAdapter(OnSubscriptionDeleteListener listener) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull Subscription oldItem, @NonNull Subscription newItem) {
                return oldItem.getSubscriptionId() == newItem.getSubscriptionId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Subscription oldItem, @NonNull Subscription newItem) {
                return oldItem.getCity().equals(newItem.getCity()) &&
                        oldItem.getDistrict().equals(newItem.getDistrict());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubscriptionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubscriptionBinding binding = ItemSubscriptionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionAdapter.ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubscriptionBinding binding;

        ViewHolder(ItemSubscriptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Subscription subscription) {
            binding.tvLocation.setText(subscription.getDisplayText());
            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(subscription);
                }
            });
        }
    }
}

