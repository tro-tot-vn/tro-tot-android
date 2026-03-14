package com.trototvn.trototandroid.ui.main.chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewbinding.ViewBinding;

import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.databinding.ItemChatReceivedBinding;
import com.trototvn.trototandroid.databinding.ItemChatSentBinding;
import com.trototvn.trototandroid.ui.base.BaseAdapter;
import com.trototvn.trototandroid.ui.base.BaseDiffCallback;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * ChatAdapter - Hiển thị danh sách tin nhắn 1-1.
 * Hỗ trợ 2 ViewType: Gửi đi (phải) và Nhận về (trái).
 * Kiến trúc: Kế thừa BaseAdapter và BaseDiffCallback.
 */
public class ChatAdapter extends BaseAdapter<MessageEntity, ViewBinding> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final long currentUserId;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter(long currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        MessageEntity message = getItem(position);
        if (message.senderId == currentUserId) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    // Override onCreateViewHolder để hỗ trợ Multi-ViewType (bỏ qua createBinding
    // của BaseAdapter)
    @NonNull
    @Override
    public BaseViewHolder<ViewBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            ItemChatSentBinding binding = ItemChatSentBinding.inflate(inflater, parent, false);
            return new BaseViewHolder<>(binding);
        } else {
            ItemChatReceivedBinding binding = ItemChatReceivedBinding.inflate(inflater, parent, false);
            return new BaseViewHolder<>(binding);
        }
    }

    // Override onBindViewHolder để ép kiểu ViewBinding tương ứng cho từng loại tin
    // nhắn
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<ViewBinding> holder, int position) {
        MessageEntity message = getItem(position);
        if (holder.binding instanceof ItemChatSentBinding) {
            ItemChatSentBinding binding = (ItemChatSentBinding) holder.binding;
            binding.tvContent.setText(message.content);
            binding.tvTime.setText(timeFormat.format(message.createdAt));
            // Xử lý tick đọc/gửi nếu cần
        } else if (holder.binding instanceof ItemChatReceivedBinding) {
            ItemChatReceivedBinding binding = (ItemChatReceivedBinding) holder.binding;
            binding.tvContent.setText(message.content);
            binding.tvTime.setText(timeFormat.format(message.createdAt));
            // Avatar xử lý sau
        }
    }

    @Override
    protected ViewBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
        // Không dùng trong trường hợp Multi-ViewType vì onCreateViewHolder đã được ghi
        // đè
        return null;
    }

    @Override
    protected void bind(ViewBinding binding, MessageEntity item, int position) {
        // Không dùng trong trường hợp Multi-ViewType vì onBindViewHolder đã được ghi đè
    }

    @Override
    protected DiffUtil.Callback createDiffCallback(List<MessageEntity> oldList, List<MessageEntity> newList) {
        return new BaseDiffCallback<>(oldList, newList) {
            @Override
            protected boolean areItemsTheSame(MessageEntity oldItem, MessageEntity newItem) {
                return oldItem.messageId == newItem.messageId; // Kiểm tra bằng khóa chính
            }

            @Override
            protected boolean areContentsTheSame(MessageEntity oldItem, MessageEntity newItem) {
                // Kiểm tra nội dung thay đổi
                return oldItem.content.equals(newItem.content) &&
                        oldItem.messageStatus.equals(newItem.messageStatus);
            }
        };
    }
}
