package com.trototvn.trototandroid.ui.main.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DiffUtil;

import com.trototvn.trototandroid.data.local.entity.ConversationEntity;
import com.trototvn.trototandroid.databinding.ItemConversationBinding;
import com.trototvn.trototandroid.ui.base.BaseAdapter;
import com.trototvn.trototandroid.ui.base.BaseDiffCallback;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * ConversationAdapter - Hiển thị danh sách các cuộc hội thoại (Inbox).
 * Kiến trúc: Kế thừa BaseAdapter và BaseDiffCallback.
 */
public class ConversationAdapter extends BaseAdapter<ConversationEntity, ItemConversationBinding> {

    private final OnConversationClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnConversationClickListener {
        void onConversationClick(long conversationId, String partnerName);
    }

    public ConversationAdapter(OnConversationClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected ItemConversationBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
        return ItemConversationBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void bind(ItemConversationBinding binding, ConversationEntity conversation, int position) {
        binding.tvName.setText(conversation.partnerName);
        
        if (conversation.lastMessage != null && !conversation.lastMessage.trim().isEmpty()) {
            binding.tvLastMessage.setText(conversation.lastMessage);
            binding.tvLastMessage.setVisibility(View.VISIBLE);
        } else {
            binding.tvLastMessage.setText("Chưa có tin nhắn");
            binding.tvLastMessage.setVisibility(View.VISIBLE);
        }

        if (conversation.updatedAt != null) {
            java.util.Calendar calUpdated = java.util.Calendar.getInstance();
            calUpdated.setTime(conversation.updatedAt);
            java.util.Calendar calNow = java.util.Calendar.getInstance();

            if (calUpdated.get(java.util.Calendar.YEAR) == calNow.get(java.util.Calendar.YEAR) &&
                calUpdated.get(java.util.Calendar.DAY_OF_YEAR) == calNow.get(java.util.Calendar.DAY_OF_YEAR)) {
                binding.tvTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(conversation.updatedAt));
            } else {
                binding.tvTime.setText(new SimpleDateFormat("dd/MM", Locale.getDefault()).format(conversation.updatedAt));
            }
        } else {
            binding.tvTime.setText("");
        }

        // Badge unread
        if (conversation.unreadCount > 0) {
            binding.badgeUnread.setVisibility(View.VISIBLE);
            binding.badgeUnread.setText(String.valueOf(conversation.unreadCount));
            // Phông chữ đậm cho tên và tin nhắn khi chưa đọc (optional style)
            binding.tvName.setAlpha(1.0f);
            binding.tvLastMessage.setAlpha(1.0f);
        } else {
            binding.badgeUnread.setVisibility(View.GONE);
            // Làm mờ nhẹ text nếu đã đọc (optional)
        }

        binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation.conversationId, conversation.partnerName);
            }
        });

        // Avatar Handling
        com.bumptech.glide.Glide.with(binding.getRoot().getContext())
                .load(conversation.partnerAvatar)
                .placeholder(com.trototvn.trototandroid.R.drawable.ic_user)
                .error(com.trototvn.trototandroid.R.drawable.ic_user)
                .circleCrop()
                .into(binding.ivAvatar);
    }

    @Override
    protected DiffUtil.Callback createDiffCallback(List<ConversationEntity> oldList, List<ConversationEntity> newList) {
        return new BaseDiffCallback<ConversationEntity>(oldList, newList) {
            @Override
            protected boolean areItemsTheSame(ConversationEntity oldItem, ConversationEntity newItem) {
                return oldItem.conversationId == newItem.conversationId;
            }

            @Override
            protected boolean areContentsTheSame(ConversationEntity oldItem, ConversationEntity newItem) {
                return oldItem.updatedAt.equals(newItem.updatedAt) &&
                        oldItem.unreadCount == newItem.unreadCount &&
                        oldItem.lastMessage.equals(newItem.lastMessage);
            }
        };
    }
}
