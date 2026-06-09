package com.trototvn.trototandroid.ui.main.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trototvn.trototandroid.App;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.data.local.entity.MessageStatus;
import com.trototvn.trototandroid.di.GlideApp;
import com.trototvn.trototandroid.ui.videocall.VideoCallActivity;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.repository.ChatRepository;
import com.trototvn.trototandroid.databinding.FragmentChatDetailBinding;
import com.trototvn.trototandroid.ui.base.BaseFragment;
import com.trototvn.trototandroid.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * ChatFragment - Màn hình chi tiết cuộc trò chuyện 1-1.
 * Tích hợp pagination, Socket.io và SSOT qua Room.
 */
@AndroidEntryPoint
public class ChatFragment extends BaseFragment<FragmentChatDetailBinding> {

    private static final String ARG_CONVERSATION_ID = "conversationId";
    private static final String ARG_PARTNER_NAME = "partnerName";
    private static final int PAGE_SIZE = 20;

    @Inject
    SessionManager sessionManager;

    private ChatViewModel viewModel;
    private ChatAdapter adapter;
    private long conversationId;
    private boolean isInitialLoad = true;

    private ActivityResultLauncher<String> imagePicker;

    /**
     * Factory method để tạo instance mới
     */
    public static ChatFragment newInstance(long conversationId, String partnerName) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CONVERSATION_ID, conversationId);
        args.putString(ARG_PARTNER_NAME, partnerName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        if (getArguments() != null) {
            conversationId = getArguments().getLong(ARG_CONVERSATION_ID);
            viewModel.init(conversationId);
        }

        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && getContext() != null) {
                viewModel.sendImage(getContext(), uri);
            }
        });
    }

    @Override
    protected void setupViews() {
        // Toolbar info
        if (getArguments() != null) {
            binding.toolbarTitle.setText(getArguments().getString(ARG_PARTNER_NAME));
        }

        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        // Initialize Adapter
        long currentUserId = 0;
        try {
            String uid = sessionManager.getUserId();
            if (uid != null)
                currentUserId = Long.parseLong(uid);
        } catch (NumberFormatException e) {
            Timber.e(e, "Invalid userId format in session");
        }

        adapter = new ChatAdapter(currentUserId);
        adapter.setOnMessageDeleteListener(message -> {
            viewModel.deleteMessage(message.messageId);
        });

        // RecyclerView Setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.rvChat.setLayoutManager(layoutManager);
        binding.rvChat.setAdapter(adapter);

        // Pagination Scroll Listener
        binding.rvChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Nếu cuộn lên trên cùng (dy < 0) và list ko đang tải
                if (dy < 0 && layoutManager.findFirstVisibleItemPosition() == 0) {
                    viewModel.loadMoreOldMessages(PAGE_SIZE);
                }
            }
        });

        // Event: Gửi tin nhắn
        binding.btnSend.setOnClickListener(v -> {
            String content = Objects.requireNonNull(binding.etMessage.getText()).toString().trim();
            if (!content.isEmpty()) {
                viewModel.sendTextMessage(content);
                binding.etMessage.setText("");
            }
        });

        binding.btnAttach.setOnClickListener(v -> {
            imagePicker.launch("image/*");
        });

        // Event: Gọi điện thoại video
        binding.btnCall.setOnClickListener(v -> {
            String partnerName = getArguments() != null ? getArguments().getString(ARG_PARTNER_NAME) : getString(com.trototvn.trototandroid.R.string.default_user_name);
            viewModel.startCall(partnerName);
        });
    }

    @Override
    protected void observeData() {
        // Observe messages từ Room (SSOT)
        viewModel.getChatMessagesLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                List<MessageEntity> messages = resource.getData();
                if (messages != null) {
                    boolean shouldScrollToBottom = isInitialLoad || isLastItemVisible();

                    adapter.submitList(messages);

                    if (shouldScrollToBottom) {
                        // BaseAdapter.submitList evaluates synchronously on main thread,
                        // so we can scroll immediately after updating the list.
                        binding.rvChat.scrollToPosition(adapter.getItemCount() - 1);
                    }

                    if (isInitialLoad && !messages.isEmpty()) {
                        isInitialLoad = false;
                    }

                    long currentUserId = 0;
                    try {
                        String uid = viewModel.getCurrentUserId();
                        if (uid != null) {
                            currentUserId = Long.parseLong(uid);
                        }
                    } catch (NumberFormatException ignored) {
                    }

                    List<Long> unreadIds = new ArrayList<>();
                    for (MessageEntity msg : messages) {
                        if (msg.senderId != currentUserId && !MessageStatus.READ.equals(msg.messageStatus)) {
                            unreadIds.add(msg.messageId);
                        }
                    }
                    if (!unreadIds.isEmpty()) {
                        viewModel.markAsRead(unreadIds);
                    }
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                showToast(resource.getMessage());
            }
        });

        // Observe trạng thái load more (ẩn/hiện loading indicator nếu có)
        viewModel.getLoadMoreStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.LOADING) {
                // Hiển thị thanh loading ở trên cùng nếu bạn có
            }
        });

        // Observe upload state
        viewModel.getUploadState().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.btnSend.setEnabled(false);
                    binding.btnAttach.setEnabled(false);
                    // Có thể hiển thị progress nhỏ cạnh nút gửi
                    break;
                case SUCCESS:
                    binding.btnSend.setEnabled(true);
                    binding.btnAttach.setEnabled(true);
                    binding.etMessage.setText("");
                    break;
                case ERROR:
                    binding.btnSend.setEnabled(true);
                    binding.btnAttach.setEnabled(true);
                    showToast(resource.getMessage());
                    break;
            }
        });

        // Quan sát thông tin hội thoại để tải ảnh đại diện đối phương
        viewModel.getConversationLiveData().observe(getViewLifecycleOwner(), conversation -> {
            if (conversation != null) {
                // Cập nhật avatar đối phương vào Adapter tin nhắn
                adapter.setPartnerAvatar(conversation.partnerAvatar);

                // Hiển thị ảnh đại diện trên Toolbar
                if (conversation.partnerAvatar != null && !conversation.partnerAvatar.isEmpty()) {
                    GlideApp.with(this)
                            .load(conversation.partnerAvatar)
                            .placeholder(com.trototvn.trototandroid.R.drawable.ic_default_avatar)
                            .error(com.trototvn.trototandroid.R.drawable.ic_default_avatar)
                            .circleCrop()
                            .into(binding.toolbarAvatar);
                } else {
                    binding.toolbarAvatar.setImageResource(com.trototvn.trototandroid.R.drawable.ic_default_avatar);
                }
            }
        });

        // Quan sát trạng thái tạo phòng gọi để mở VideoCallActivity
        viewModel.getCallRoomLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.btnCall.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.btnCall.setEnabled(true);
                    if (resource.getData() != null) {
                        ChatRepository.CallRoomInfo callInfo = resource.getData();
                        Intent intent = new Intent(getContext(), VideoCallActivity.class);
                        intent.putExtra("roomId", callInfo.roomId);
                        intent.putExtra("partnerId", callInfo.partnerId);
                        intent.putExtra("partnerName", callInfo.partnerName);
                        intent.putExtra("isCaller", true);
                        startActivity(intent);
                        viewModel.resetCallRoomState();
                    }
                    break;
                case ERROR:
                    binding.btnCall.setEnabled(true);
                    showToast(resource.getMessage());
                    viewModel.resetCallRoomState();
                    break;
            }
        });
    }

    /**
     * Kiểm tra xem item cuối có đang hiển thị không trước khi auto-scroll
     */
    private boolean isLastItemVisible() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rvChat.getLayoutManager();
        if (layoutManager == null || adapter.getItemCount() == 0)
            return false;

        int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
        return lastVisiblePosition >= adapter.getItemCount() - 2;
    }

    @Override
    public void onResume() {
        super.onResume();
        App.activeConversationId = String.valueOf(conversationId);
        viewModel.joinConversation(conversationId);
    }

    @Override
    public void onPause() {
        super.onPause();
        App.activeConversationId = null;
    }
}
