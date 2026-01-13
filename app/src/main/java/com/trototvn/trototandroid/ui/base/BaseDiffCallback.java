package com.trototvn.trototandroid.ui.base;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

/**
 * Base DiffUtil Callback for comparing lists
 * 
 * @param <T> Data type
 */
public abstract class BaseDiffCallback<T> extends DiffUtil.Callback {

    private final List<T> oldList;
    private final List<T> newList;

    public BaseDiffCallback(List<T> oldList, List<T> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return areItemsTheSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return areContentsTheSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }

    /**
     * Compare if two items represent the same object (usually by ID)
     */
    protected abstract boolean areItemsTheSame(T oldItem, T newItem);

    /**
     * Compare if two items have the same content
     */
    protected abstract boolean areContentsTheSame(T oldItem, T newItem);
}
