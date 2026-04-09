package com.trototvn.trototandroid.ui.main.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.content.Context;
import android.net.Uri;

import com.trototvn.trototandroid.data.local.entity.MessageAttachmentEntity;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.data.local.entity.MessageType;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.repository.ChatRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;
import com.trototvn.trototandroid.utils.SessionManager;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * ChatViewModel - Quản lý logic UI cho màn hình Chat 1-1.
 * Tuân thủ chuẩn Single Source of Truth (SSOT).
 */
@HiltViewModel
public class ChatViewModel extends BaseViewModel {

    private final ChatRepository chatRepository;
    private final SessionManager sessionManager;

    private long conversationId = -1;

    // LiveData quan trọng nhất: Stream tin nhắn từ Room
    private final MutableLiveData<Resource<List<MessageEntity>>> chatMessagesLiveData = new MutableLiveData<>();

    // LiveData thông báo trạng thái tải thêm tin cũ
    private final MutableLiveData<Boolean> hasMoreMessages = new MutableLiveData<>(true);
    private final MutableLiveData<Resource<Boolean>> loadMoreStatus = new MutableLiveData<>();
    
    private final MutableLiveData<Resource<MessageEntity>> uploadState = new MutableLiveData<>();

    @Inject
    public ChatViewModel(ChatRepository chatRepository, SessionManager sessionManager) {
        this.chatRepository = chatRepository;
        this.sessionManager = sessionManager;
    }

    /**
     * Khởi tạo ViewModel với conversationId.
     * Nên gọi từ Activity/Fragment ngay sau khi khởi tạo.
     */
    public void init(long conversationId) {
        this.conversationId = conversationId;
        fetchInitialHistory();
        observeChatData();
        // Bắt đầu lắng nghe tin nhắn mới qua socket
        chatRepository.observeIncomingMessages();
    }

    /**
     * Kéo lịch sử tin nhắn mới nhất từ server về Room
     */
    private void fetchInitialHistory() {
        addDisposable(chatRepository.fetchChatHistory(conversationId, 20, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        hasMore -> hasMoreMessages.setValue(hasMore),
                        error -> Timber.e(error, "Failed to fetch initial chat history")));
    }

    /**
     * [SSOT] Lắng nghe thay đổi dữ liệu từ Room DB.
     * Mọi thay đổi (từ Socket, API, FCM) đều sẽ trigger UI update thông qua đây.
     */
    public void observeChatData() {
        if (conversationId == -1)
            return;

        addDisposable(chatRepository.observeMessages(conversationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        messages -> handleSuccess(chatMessagesLiveData, messages),
                        error -> {
                            Timber.e(error, "Observe chat data error");
                            handleError(chatMessagesLiveData, "Không thể tải tin nhắn cục bộ");
                        }));
    }

    /**
     * Phân trang: Tải tin nhắn cũ dựa trên offset (số lượng tin hiện có)
     */
    public void loadMoreOldMessages(int limit) {
        if (conversationId == -1 || Boolean.FALSE.equals(hasMoreMessages.getValue()))
            return;

        // Offset = số lượng tin nhắn đang hiển thị
        int offset = 0;
        if (chatMessagesLiveData.getValue() != null && chatMessagesLiveData.getValue().getData() != null) {
            offset = chatMessagesLiveData.getValue().getData().size();
        }

        handleLoading(loadMoreStatus);

        addDisposable(chatRepository.fetchChatHistory(conversationId, limit, offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        hasMore -> {
                            hasMoreMessages.setValue(hasMore);
                            handleSuccess(loadMoreStatus, hasMore);
                        },
                        error -> {
                            Timber.e(error, "Load more messages error");
                            handleError(loadMoreStatus, "Không thể tải thêm tin nhắn");
                        }));
    }

    /**
     * Gửi tin nhắn văn bản
     */
    public void sendTextMessage(String text) {
        if (conversationId == -1 || text == null || text.trim().isEmpty())
            return;

        addDisposable(chatRepository.sendTextMessage(conversationId, text.trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Timber.d("Message sent successfully"),
                        error -> Timber.e(error, "Send message failed")));
    }

    /**
     * Gửi tin nhắn file/ảnh
     */
    public void sendFileMessage(String content, @MessageType String type, MessageAttachmentEntity attachment) {
        if (conversationId == -1)
            return;

        addDisposable(chatRepository.sendFileMessage(conversationId, content, type, attachment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Timber.d("File message sent successfully"),
                        error -> Timber.e(error, "Send file message failed")));
    }

    /**
     * Tải file lên và gửi tin nhắn
     */
    public void sendImage(Context context, Uri uri) {
        if (conversationId == -1 || uri == null) return;
        
        handleLoading(uploadState);
        
        addDisposable(chatRepository.uploadMediaAndSendMessage(conversationId, uri, "", context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        entity -> handleSuccess(uploadState, entity),
                        error -> {
                            Timber.e(error, "Send image failed");
                            handleError(uploadState, "Lỗi khi gửi ảnh: " + error.getMessage());
                        }
                ));
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    public void markAsRead(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) return;

        addDisposable(chatRepository.markMessagesAsRead(messageIds)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> Timber.d("Marked %d messages as read", messageIds.size()),
                        error -> Timber.e(error, "Failed to mark messages as read")
                )
        );
    }

    // Getters
    public LiveData<Resource<List<MessageEntity>>> getChatMessagesLiveData() {
        return chatMessagesLiveData;
    }

    public LiveData<Resource<Boolean>> getLoadMoreStatus() {
        return loadMoreStatus;
    }

    public LiveData<Resource<MessageEntity>> getUploadState() {
        return uploadState;
    }

    public LiveData<Boolean> getHasMoreMessages() {
        return hasMoreMessages;
    }

    public String getCurrentUserId() {
        return sessionManager.getUserId();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Dừng lắng nghe socket khi ViewModel bị hủy
        chatRepository.stopObservingIncomingMessages();
    }
}
