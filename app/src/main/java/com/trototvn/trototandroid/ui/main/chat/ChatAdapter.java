package com.trototvn.trototandroid.ui.main.chat;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textview.MaterialTextView;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    public interface OnMessageDeleteListener {
        void onDelete(MessageEntity message);
    }

    private OnMessageDeleteListener deleteListener;

    public void setOnMessageDeleteListener(OnMessageDeleteListener listener) {
        this.deleteListener = listener;
    }

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

            boolean isLastMessage = (position == getItemCount() - 1);
            String timeString = timeFormat.format(message.createdAt);

            if (isLastMessage) {
                String statusText = "";
                String rawStatus = message.messageStatus;

                if (rawStatus != null) {
                    switch (rawStatus.toUpperCase()) {
                        case "READ":
                            statusText = " • Đã xem";
                            break;
                        case "DELIVERED":
                            statusText = " • Đã nhận";
                            break;
                        case "ERROR":
                            statusText = " • Lỗi";
                            break;
                        case "SENT":
                        default:
                            statusText = " • Đã gửi";
                            break;
                    }
                }
                binding.tvTime.setText(timeString + statusText);

                if ("ERROR".equalsIgnoreCase(rawStatus)) {
                    binding.tvTime.setTextColor(Color.parseColor("#F44336"));
                } else {
                    binding.tvTime.setTextColor(Color.parseColor("#888888"));
                }
            } else {
                binding.tvTime.setText(timeString);
                binding.tvTime.setTextColor(Color.parseColor("#888888"));
            }

            binding.tvMessageStatus.setVisibility(View.GONE);

            binding.cvMessage.setOnLongClickListener(v -> {
                showMessageContextMenu(v.getContext(), message, true);
                return true;
            });
        } else if (holder.binding instanceof ItemChatReceivedBinding) {
            ItemChatReceivedBinding binding = (ItemChatReceivedBinding) holder.binding;
            bindDateHeader(binding.tvDateHeader, message, position);
            binding.tvContent.setText(message.content);
            binding.tvTime.setText(timeFormat.format(message.createdAt));
            // Avatar xử lý sau

            binding.cvMessage.setOnLongClickListener(v -> {
                showMessageContextMenu(v.getContext(), message, false);
                return true;
            });
        } else if (holder.binding instanceof ItemChatImageSentBinding) {
            ItemChatImageSentBinding binding = (ItemChatImageSentBinding) holder.binding;
            bindDateHeader(binding.tvDateHeader, message, position);

            boolean isLastMessage = (position == getItemCount() - 1);
            String timeString = timeFormat.format(message.createdAt);

            if (isLastMessage) {
                String statusText = "";
                String rawStatus = message.messageStatus;

                if (rawStatus != null) {
                    switch (rawStatus.toUpperCase()) {
                        case "READ":
                            statusText = " • Đã xem";
                            break;
                        case "DELIVERED":
                            statusText = " • Đã nhận";
                            break;
                        case "ERROR":
                            statusText = " • Lỗi";
                            break;
                        case "SENT":
                        default:
                            statusText = " • Đã gửi";
                            break;
                    }
                }
                binding.tvTime.setText(timeString + statusText);

                if ("ERROR".equalsIgnoreCase(rawStatus)) {
                    binding.tvTime.setTextColor(Color.parseColor("#F44336"));
                } else {
                    binding.tvTime.setTextColor(Color.parseColor("#888888"));
                }
            } else {
                binding.tvTime.setText(timeString);
                binding.tvTime.setTextColor(Color.parseColor("#888888"));
            }

            binding.tvMessageStatus.setVisibility(View.GONE);

            binding.ivContent.setOnLongClickListener(v -> {
                showMessageContextMenu(v.getContext(), message, true);
                return true;
            });

            String fileUrl = "";
            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                fileUrl = message.getAttachments().get(0).fileUrl;
            }

            if (fileUrl != null && !fileUrl.isEmpty() && !fileUrl.startsWith("http")) {
                String baseUrl = Constants.BASE_URL;

                if (baseUrl.endsWith("/") && fileUrl.startsWith("/")) {
                    fileUrl = baseUrl + fileUrl.substring(1);
                } else if (!baseUrl.endsWith("/") && !fileUrl.startsWith("/")) {
                    fileUrl = baseUrl + "/" + fileUrl;
                } else {
                    fileUrl = baseUrl + fileUrl;
                }
            }

            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                Glide.with(binding.getRoot().getContext()).clear(binding.ivContent);
                binding.ivContent.setImageResource(R.drawable.ic_image_placeholder);
            } else {
                Glide.with(binding.getRoot().getContext())
                        .load(fileUrl)
                        .placeholder(new ColorDrawable(Color.LTGRAY))
                        .error(new ColorDrawable(Color.RED))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivContent);

                String finalUrl = fileUrl;
                binding.ivContent.setOnClickListener(v -> showFullScreenImage(v.getContext(), finalUrl));
            }

        } else if (holder.binding instanceof ItemChatImageReceivedBinding) {
            ItemChatImageReceivedBinding binding = (ItemChatImageReceivedBinding) holder.binding;
            bindDateHeader(binding.tvDateHeader, message, position);
            binding.tvTime.setText(timeFormat.format(message.createdAt));

            binding.ivContent.setOnLongClickListener(v -> {
                showMessageContextMenu(v.getContext(), message, false);
                return true;
            });

            String fileUrl = "";
            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                fileUrl = message.getAttachments().get(0).fileUrl;
            }

            if (fileUrl != null && !fileUrl.isEmpty() && !fileUrl.startsWith("http")) {
                String baseUrl = Constants.BASE_URL;

                if (baseUrl.endsWith("/") && fileUrl.startsWith("/")) {
                    fileUrl = baseUrl + fileUrl.substring(1);
                } else if (!baseUrl.endsWith("/") && !fileUrl.startsWith("/")) {
                    fileUrl = baseUrl + "/" + fileUrl;
                } else {
                    fileUrl = baseUrl + fileUrl;
                }
            }

            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                Glide.with(binding.getRoot().getContext()).clear(binding.ivContent);
                binding.ivContent.setImageResource(R.drawable.ic_image_placeholder);
            } else {
                Glide.with(binding.getRoot().getContext())
                        .load(fileUrl)
                        .placeholder(new ColorDrawable(Color.LTGRAY))
                        .error(new ColorDrawable(Color.RED))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivContent);

                String finalUrl = fileUrl;
                binding.ivContent.setOnClickListener(v -> showFullScreenImage(v.getContext(), finalUrl));
            }
        }
    }

    private void bindDateHeader(MaterialTextView tvDateHeader, MessageEntity message, int position) {
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

    // =======================================================
    // MENU BOTTOM SHEET CHO TIN NHẮN (NHẤN GIỮ)
    // =======================================================
    private void showMessageContextMenu(Context context, MessageEntity message, boolean isSentByMe) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);

        // 1. Tạo Layout chứa các thành phần
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 50, 60, 50);

        // 2. Định dạng ngày giờ và trạng thái cho Header
        String dateTimeString = "Không rõ thời gian";
        if (message.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            dateTimeString = sdf.format(message.getCreatedAt());
        }

        String statusText = "";
        if (isSentByMe) {
            String rawStatus = message.getMessageStatus();
            if (rawStatus != null) {
                switch (rawStatus.toUpperCase()) {
                    case "READ":
                        statusText = " • Đã xem";
                        break;
                    case "DELIVERED":
                        statusText = " • Đã nhận";
                        break;
                    case "ERROR":
                        statusText = " • Lỗi gửi";
                        break;
                    case "SENT":
                    default:
                        statusText = " • Đã gửi";
                        break;
                }
            }
        }

        // 3. Tạo TextView Header (Ngày giờ + Status)
        TextView tvHeader = new TextView(context);
        tvHeader.setText(dateTimeString + statusText);
        tvHeader.setTextSize(14);
        tvHeader.setTypeface(null, Typeface.BOLD);
        tvHeader.setGravity(Gravity.CENTER);
        tvHeader.setTextColor(Color.GRAY);
        tvHeader.setPadding(0, 0, 0, 60);
        layout.addView(tvHeader);

        // 4. Tạo nút Copy (Chỉ copy text, nếu là ảnh thì báo không hỗ trợ)
        TextView tvCopy = new TextView(context);
        tvCopy.setText("Sao chép tin nhắn");
        tvCopy.setTextSize(16);
        tvCopy.setPadding(0, 40, 0, 40);
        tvCopy.setOnClickListener(v -> {
            if (message.getContent() != null && !message.getContent().isEmpty()) {
                // Logic copy text vào Clipboard
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Message", message.getContent());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Đã sao chép vào khay nhớ tạm", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Chỉ hỗ trợ sao chép văn bản", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });
        layout.addView(tvCopy);

        // 5. Tạo nút Delete (Màu đỏ)
        if (isSentByMe) {
            TextView tvDelete = new TextView(context);
            tvDelete.setText("Xóa tin nhắn");
            tvDelete.setTextSize(16);
            tvDelete.setTextColor(Color.parseColor("#F44336")); // Màu đỏ
            tvDelete.setPadding(0, 40, 0, 40);
            tvDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(message);
                } else {
                    Toast.makeText(context, "Chức năng xóa chưa được thiết lập", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
            layout.addView(tvDelete);
        }

        // 6. Hiển thị Dialog
        dialog.setContentView(layout);
        dialog.show();
    }

    private void showFullScreenImage(Context context, String imageUrl) {
        Dialog dialog = new android.app.Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(new ColorDrawable(Color.LTGRAY))
                .error(new ColorDrawable(Color.RED))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        imageView.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(imageView);
        dialog.show();
    }
}
