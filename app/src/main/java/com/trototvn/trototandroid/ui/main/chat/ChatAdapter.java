package com.trototvn.trototandroid.ui.main.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewbinding.ViewBinding;

import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.data.local.entity.MessageStatus;
import com.trototvn.trototandroid.data.local.entity.MessageType;
import com.trototvn.trototandroid.databinding.ItemChatImageReceivedBinding;
import com.trototvn.trototandroid.databinding.ItemChatImageSentBinding;
import com.trototvn.trototandroid.databinding.ItemChatReceivedBinding;
import com.trototvn.trototandroid.databinding.ItemChatSentBinding;
import com.trototvn.trototandroid.ui.base.BaseAdapter;
import com.trototvn.trototandroid.ui.base.BaseDiffCallback;
import com.trototvn.trototandroid.utils.Constants;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * ChatAdapter - Hiển thị danh sách tin nhắn 1-1.
 * Hỗ trợ 2 ViewType: Gửi đi (phải) và Nhận về (trái).
 * Kiến trúc: Kế thừa BaseAdapter và BaseDiffCallback.
 */
public class ChatAdapter extends BaseAdapter<MessageEntity, ViewBinding> {

    private static final int VIEW_TYPE_TEXT_SENT = 1;
    private static final int VIEW_TYPE_TEXT_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;

    private final long currentUserId;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter(long currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        MessageEntity message = getItem(position);
        boolean isSent = message.senderId == currentUserId;
        boolean isImage = MessageType.IMAGE.equals(message.messageType);

        if (isImage) {
            return isSent ? VIEW_TYPE_IMAGE_SENT : VIEW_TYPE_IMAGE_RECEIVED;
        } else {
            return isSent ? VIEW_TYPE_TEXT_SENT : VIEW_TYPE_TEXT_RECEIVED;
        }
    }

    // Override onCreateViewHolder để hỗ trợ Multi-ViewType (bỏ qua createBinding
    // của BaseAdapter)
    @NonNull
    @Override
    public BaseViewHolder<ViewBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_TEXT_SENT) {
            ItemChatSentBinding binding = ItemChatSentBinding.inflate(inflater, parent, false);
            return new BaseViewHolder<>(binding);
        } else if (viewType == VIEW_TYPE_TEXT_RECEIVED) {
            ItemChatReceivedBinding binding = ItemChatReceivedBinding.inflate(inflater, parent, false);
            return new BaseViewHolder<>(binding);
        } else if (viewType == VIEW_TYPE_IMAGE_SENT) {
            ItemChatImageSentBinding binding = ItemChatImageSentBinding.inflate(inflater, parent, false);
            return new BaseViewHolder<>(binding);
        } else {
            ItemChatImageReceivedBinding binding = ItemChatImageReceivedBinding.inflate(inflater, parent, false);
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
            bindDateHeader(binding.tvDateHeader, message, position);
            binding.tvContent.setText(message.content);
            binding.tvTime.setText(timeFormat.format(message.createdAt));
            // Xử lý tick đọc/gửi nếu cần

            boolean showStatus = MessageStatus.ERROR.equals(message.messageStatus);
            if (!showStatus) {
                if (position == getItemCount() - 1) {
                    showStatus = true;
                } else {
                    MessageEntity nextMsg = getItem(position + 1);
                    if (nextMsg.senderId != message.senderId) {
                        showStatus = true;
                    }
                }
            }

            if (showStatus) {
                binding.tvMessageStatus.setVisibility(View.VISIBLE);
                if (MessageStatus.ERROR.equals(message.messageStatus)) {
                    binding.tvMessageStatus.setText("Gửi không thành công!");
                    binding.tvMessageStatus.setTextColor(Color.parseColor("#F44336"));
                } else if (MessageStatus.READ.equals(message.messageStatus)) {
                    binding.tvMessageStatus.setText("Đã xem");
                    binding.tvMessageStatus.setTextColor(Color.parseColor("#888888"));
                } else {
                    binding.tvMessageStatus.setText("Đã gửi");
                    binding.tvMessageStatus.setTextColor(Color.parseColor("#888888"));
                }
            } else {
                binding.tvMessageStatus.setVisibility(View.GONE);
            }
        } else if (holder.binding instanceof ItemChatReceivedBinding) {
            ItemChatReceivedBinding binding = (ItemChatReceivedBinding) holder.binding;
            bindDateHeader(binding.tvDateHeader, message, position);
            binding.tvContent.setText(message.content);
            binding.tvTime.setText(timeFormat.format(message.createdAt));
            // Avatar xử lý sau
        } else if (holder.binding instanceof ItemChatImageSentBinding) {
            ItemChatImageSentBinding binding = (ItemChatImageSentBinding) holder.binding;
            bindDateHeader(binding.tvDateHeader, message, position);
            binding.tvTime.setText(timeFormat.format(message.createdAt));

            boolean showStatus = MessageStatus.ERROR.equals(message.messageStatus);
            if (!showStatus) {
                if (position == getItemCount() - 1) {
                    showStatus = true;
                } else {
                    MessageEntity nextMsg = getItem(position + 1);
                    if (nextMsg.senderId != message.senderId) {
                        showStatus = true;
                    }
                }
            }

            if (showStatus) {
                binding.tvMessageStatus.setVisibility(View.VISIBLE);
                if (MessageStatus.ERROR.equals(message.messageStatus)) {
                    binding.tvMessageStatus.setText("Gửi không thành công!");
                    binding.tvMessageStatus.setTextColor(Color.parseColor("#F44336"));
                } else if (MessageStatus.READ.equals(message.messageStatus)) {
                    binding.tvMessageStatus.setText("Đã xem");
                    binding.tvMessageStatus.setTextColor(Color.parseColor("#888888"));
                } else {
                    binding.tvMessageStatus.setText("Đã gửi");
                    binding.tvMessageStatus.setTextColor(Color.parseColor("#888888"));
                }
            } else {
                binding.tvMessageStatus.setVisibility(View.GONE);
            }

            String url = "";
            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                url = message.getAttachments().get(0).fileUrl;
            }
            if (url != null && !url.isEmpty()) {
                if (!url.startsWith("http")) {
                    String baseUrl = Constants.BASE_URL;
                    if (baseUrl.endsWith("/")) {
                        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                    }
                    if (!url.startsWith("/")) {
                        url = "/" + url;
                    }
                    url = baseUrl + url;
                }
                // Fallback sửa lỗi URL gãy
                url = url.replace("3333//api", "3333/api");
                url = url.replace("net//api", "net/api");
            }

            Glide.with(binding.getRoot().getContext())
                    .load(url)
                    .placeholder(new ColorDrawable(Color.LTGRAY))
                    .error(new ColorDrawable(Color.RED))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivContent);

        } else if (holder.binding instanceof ItemChatImageReceivedBinding) {
            ItemChatImageReceivedBinding binding = (ItemChatImageReceivedBinding) holder.binding;
            bindDateHeader(binding.tvDateHeader, message, position);
            binding.tvTime.setText(timeFormat.format(message.createdAt));

            String url = "";
            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                url = message.getAttachments().get(0).fileUrl;
            }
            if (url != null && !url.isEmpty()) {
                if (!url.startsWith("http")) {
                    String baseUrl = Constants.BASE_URL;
                    if (baseUrl.endsWith("/")) {
                        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                    }
                    if (!url.startsWith("/")) {
                        url = "/" + url;
                    }
                    url = baseUrl + url;
                }
                // Fallback sửa lỗi URL gãy
                url = url.replace("3333//api", "3333/api");
                url = url.replace("net//api", "net/api");
            }

            Glide.with(binding.getRoot().getContext())
                    .load(url)
                    .placeholder(new ColorDrawable(Color.LTGRAY))
                    .error(new ColorDrawable(Color.RED))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivContent);
        }
    }

    private void bindDateHeader(com.google.android.material.textview.MaterialTextView tvDateHeader, MessageEntity message, int position) {
        boolean showHeader = false;
        if (position == 0) {
            showHeader = true;
        } else {
            MessageEntity prevMessage = getItem(position - 1);
            Calendar calCurrent = Calendar.getInstance();
            calCurrent.setTime(message.createdAt);
            Calendar calPrev = Calendar.getInstance();
            calPrev.setTime(prevMessage.createdAt);
            if (calCurrent.get(Calendar.YEAR) != calPrev.get(Calendar.YEAR) ||
                calCurrent.get(Calendar.DAY_OF_YEAR) != calPrev.get(Calendar.DAY_OF_YEAR)) {
                showHeader = true;
            }
        }

        if (showHeader) {
            tvDateHeader.setVisibility(View.VISIBLE);
            
            Calendar calCurrent = Calendar.getInstance();
            calCurrent.setTime(message.createdAt);
            Calendar calNow = Calendar.getInstance();

            if (calCurrent.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calCurrent.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)) {
                tvDateHeader.setText("Hôm nay");
            } else {
                calNow.add(Calendar.DAY_OF_YEAR, -1);
                if (calCurrent.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                    calCurrent.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)) {
                    tvDateHeader.setText("Hôm qua");
                } else {
                    tvDateHeader.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(message.createdAt));
                }
            }
        } else {
            tvDateHeader.setVisibility(View.GONE);
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
