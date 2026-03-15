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
        binding.tvLastMessage.setText(conversation.lastMessage);
        binding.tvTime.setText(timeFormat.format(conversation.updatedAt));

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

        // Avatar Handling (Placeholder hoặc dùng Glide/Coil)
        // binding.ivAvatar.setImageResource(...)
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
