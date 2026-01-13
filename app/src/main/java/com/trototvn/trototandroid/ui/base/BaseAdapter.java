package com.trototvn.trototandroid.ui.base;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Base RecyclerView Adapter with ViewBinding and DiffUtil support
 * 
 * @param <T>  Data type
 * @param <VB> ViewBinding type
 */
public abstract class BaseAdapter<T, VB extends ViewBinding>
        extends RecyclerView.Adapter<BaseAdapter.BaseViewHolder<VB>> {

    protected List<T> items = new ArrayList<>();

    /**
     * Create ViewBinding for item
     */
    protected abstract VB createBinding(LayoutInflater inflater, ViewGroup parent);

    /**
     * Bind data to ViewHolder
     */
    protected abstract void bind(VB binding, T item, int position);

    /**
     * Create DiffUtil callback
     */
    protected abstract DiffUtil.Callback createDiffCallback(List<T> oldList, List<T> newList);

    @NonNull
    @Override
    public BaseViewHolder<VB> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VB binding = createBinding(LayoutInflater.from(parent.getContext()), parent);
        return new BaseViewHolder<>(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<VB> holder, int position) {
        bind(holder.binding, items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Update list with DiffUtil
     */
    public void submitList(List<T> newItems) {
        DiffUtil.Callback callback = createDiffCallback(this.items, newItems);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

        this.items.clear();
        this.items.addAll(newItems);
        result.dispatchUpdatesTo(this);
    }

    /**
     * Get item at position
     */
    public T getItem(int position) {
        return items.get(position);
    }

    /**
     * Clear all items
     */
    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    /**
     * Base ViewHolder
     */
    public static class BaseViewHolder<VB extends ViewBinding> extends RecyclerView.ViewHolder {
        public final VB binding;

        public BaseViewHolder(VB binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
