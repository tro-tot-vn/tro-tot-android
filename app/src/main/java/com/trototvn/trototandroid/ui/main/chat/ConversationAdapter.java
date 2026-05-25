package com.trototvn.trototandroid.ui.main.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DiffUtil;

import com.trototvn.trototandroid.di.GlideApp;
import com.trototvn.trototandroid.data.local.entity.ConversationEntity;
import com.trototvn.trototandroid.data.local.entity.ConversationUIModel;
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
public class ConversationAdapter extends BaseAdapter<ConversationUIModel, ItemConversationBinding> {

    private final OnConversationClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final long myUserId;

    public interface OnConversationClickListener {
        void onConversationClick(long conversationId, String partnerName);
    }

    public ConversationAdapter(long myUserId, OnConversationClickListener listener) {
        this.myUserId = myUserId;
        this.listener = listener;
    }

    @Override
    protected ItemConversationBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
        return ItemConversationBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void bind(ItemConversationBinding binding, ConversationUIModel item, int position) {
        ConversationEntity conversation = item.conversation;
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

        // Unread highlighting
        boolean isUnread = false;
        if (item.lastMessageSenderId != null && item.lastMessageSenderId != myUserId) {
            isUnread = !"Read".equalsIgnoreCase(item.lastMessageStatus);
        }
                           
        if (isUnread || conversation.unreadCount > 0) {
            if (conversation.unreadCount > 0) {
                binding.badgeUnread.setVisibility(View.VISIBLE);
                binding.badgeUnread.setText(String.valueOf(conversation.unreadCount));
            } else {
                binding.badgeUnread.setVisibility(View.GONE);
            }
            
            binding.tvLastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.tvLastMessage.setTextColor(android.graphics.Color.BLACK);
            binding.tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.tvName.setAlpha(1.0f);
            binding.tvLastMessage.setAlpha(1.0f);
        } else {
            binding.badgeUnread.setVisibility(View.GONE);
            binding.tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            binding.tvLastMessage.setTextColor(android.graphics.Color.GRAY);
            binding.tvName.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation.conversationId, conversation.partnerName);
            }
        });

        // Avatar Handling
        GlideApp.with(binding.getRoot().getContext())
                .load(conversation.partnerAvatar)
                .placeholder(com.trototvn.trototandroid.R.drawable.ic_user)
                .error(com.trototvn.trototandroid.R.drawable.ic_user)
                .circleCrop()
                .into(binding.ivAvatar);
    }

    @Override
    protected DiffUtil.Callback createDiffCallback(List<ConversationUIModel> oldList, List<ConversationUIModel> newList) {
        return new BaseDiffCallback<ConversationUIModel>(oldList, newList) {
            @Override
            protected boolean areItemsTheSame(ConversationUIModel oldItem, ConversationUIModel newItem) {
                return oldItem.conversation.conversationId == newItem.conversation.conversationId;
            }

            @Override
            protected boolean areContentsTheSame(ConversationUIModel oldItem, ConversationUIModel newItem) {
                return oldItem.conversation.updatedAt.equals(newItem.conversation.updatedAt) &&
                        oldItem.conversation.unreadCount == newItem.conversation.unreadCount &&
                        oldItem.conversation.lastMessage.equals(newItem.conversation.lastMessage) &&
                        java.util.Objects.equals(oldItem.lastMessageSenderId, newItem.lastMessageSenderId) &&
                        java.util.Objects.equals(oldItem.lastMessageStatus, newItem.lastMessageStatus);
            }
        };
    }
}
