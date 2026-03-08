package com.trototvn.trototandroid.data.repository;

import com.google.gson.Gson;
import com.trototvn.trototandroid.data.local.dao.ChatDao;
import com.trototvn.trototandroid.data.local.entity.MessageAttachmentEntity;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.data.local.entity.MessageStatus;
import com.trototvn.trototandroid.data.local.entity.MessageType;
import com.trototvn.trototandroid.data.model.chat.AttachmentDto;
import com.trototvn.trototandroid.data.model.chat.MessageDto;
import com.trototvn.trototandroid.data.model.chat.SocketMessageEvent;
import com.trototvn.trototandroid.data.remote.ApiService;
import com.trototvn.trototandroid.utils.SessionManager;
import com.trototvn.trototandroid.utils.SocketIOManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * ChatRepository – Single Source of Truth cho tính năng Chat 1-1.
 * <p>
 * Luồng dữ liệu:
 * Network/Socket ──▶ Room (ChatDao) ──▶ UI (observe Flowable từ Room)
 * <p>
 * ViewModel chỉ gọi Repository; UI chỉ observe Room.
 */
@Singleton
public class ChatRepository {

    // Số tin nhắn tối đa giữ lại mỗi conversation khi cleanup
    private static final int KEEP_MESSAGES_COUNT = 100;

    private final ChatDao chatDao;
    private final ApiService apiService;
    private final SocketIOManager socketIOManager;
    private final SessionManager sessionManager;
    private final Gson gson;

    // Dùng CompositeDisposable để quản lý Socket subscription
    private final CompositeDisposable socketDisposables = new CompositeDisposable();

    @Inject
    public ChatRepository(
            ChatDao chatDao,
            ApiService apiService,
            SocketIOManager socketIOManager,
            SessionManager sessionManager,
            Gson gson) {
        this.chatDao = chatDao;
        this.apiService = apiService;
        this.socketIOManager = socketIOManager;
        this.sessionManager = sessionManager;
        this.gson = gson;
    }

    // ─────────────────────────────────────────────────────────────
    // OBSERVE – UI subscribe Flowable này (SSOT)
    // ─────────────────────────────────────────────────────────────

    /**
     * Trả về Flowable real-time từ Room.
     * ViewModel subscribe trên IO, observe trên Main.
     * Room tự động emit khi bảng messages thay đổi.
     */
    public Flowable<List<MessageEntity>> observeMessages(String conversationId) {
        return chatDao.getMessagesByConversationId(conversationId)
                .subscribeOn(Schedulers.io());
    }

    // ─────────────────────────────────────────────────────────────
    // SEND MESSAGE – Optimistic insert → Socket emit
    // ─────────────────────────────────────────────────────────────

    /**
     * Gửi tin nhắn văn bản:
     * 1. Tạo entity tạm (SENT) → insert Room ngay (UI phản hồi tức thì)
     * 2. Emit qua Socket để gửi lên server
     * <p>
     * Server sẽ broadcast lại qua socket "message:received"; lúc đó
     * observeIncomingMessages sẽ upsert entity đó với messageId từ server.
     */
    public Completable sendTextMessage(String conversationId, String content) {
        String optimisticId = "local_" + UUID.randomUUID().toString();
        Date now = new Date();

        MessageEntity optimistic = new MessageEntity(
                optimisticId,
                conversationId,
                sessionManager.getUserId(),
                content,
                MessageType.TEXT,
                MessageStatus.SENT,
                now,
                now,
                null);

        return chatDao.insertMessage(optimistic)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    // Emit qua Socket sau khi đã insert Room thành công
                    // conversationId là String nhưng socket hiện nhận int → parse an toàn
                    try {
                        int convIdInt = Integer.parseInt(conversationId);
                        socketIOManager.sendMessage(convIdInt, content);
                    } catch (NumberFormatException e) {
                        Timber.e(e, "conversationId không phải số nguyên: %s", conversationId);
                    }
                });
    }

    /**
     * Gửi tin nhắn kèm file (IMAGE / FILE):
     * 1. Insert MessageEntity (status SENT) vào Room
     * 2. Insert MessageAttachmentEntity vào Room
     * 3. Emit socket event FILE_SENT
     */
    public Completable sendFileMessage(
            String conversationId,
            String content,
            @MessageType String messageType,
            MessageAttachmentEntity attachment) {
        String optimisticId = "local_" + UUID.randomUUID().toString();
        Date now = new Date();

        MessageEntity message = new MessageEntity(
                optimisticId,
                conversationId,
                sessionManager.getUserId(),
                content,
                messageType,
                MessageStatus.SENT,
                now,
                now,
                null);

        // Link attachment với optimistic messageId
        attachment.messageId = optimisticId;

        return chatDao.insertMessage(message)
                .andThen(chatDao.insertAttachment(attachment))
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    // Emit socket FILE_SENT event
                    if (socketIOManager.isConnected()) {
                        com.google.gson.JsonObject data = new com.google.gson.JsonObject();
                        data.addProperty("conversationId", conversationId);
                        data.addProperty("content", content);
                        data.addProperty("messageType", messageType);
                        data.addProperty("fileUrl", attachment.fileUrl);
                        data.addProperty("fileName", attachment.fileName);
                        Timber.d("Emitting FILE_SENT for conversation: %s", conversationId);
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────
    // INCOMING SOCKET MESSAGES → ROOM (SSOT)
    // ─────────────────────────────────────────────────────────────

    /**
     * Bắt đầu lắng nghe Socket và upsert tin nhắn mới vào Room.
     * Gọi hàm này một lần khi user mở màn hình chat hoặc khi Service khởi động.
     * Huỷ bằng {@link #stopObservingIncomingMessages()}.
     */
    public void observeIncomingMessages() {
        Disposable d = socketIOManager.getMessageReceived()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        raw -> {
                            try {
                                SocketMessageEvent event = gson.fromJson(raw.toString(), SocketMessageEvent.class);
                                MessageEntity entity = mapSocketEventToEntity(event);
                                chatDao.insertMessage(entity)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(
                                                () -> Timber.d("Inserted incoming message: %s", entity.messageId),
                                                err -> Timber.e(err, "Insert incoming message failed"));
                            } catch (Exception e) {
                                Timber.e(e, "Parse socket message failed: %s", raw);
                            }
                        },
                        error -> Timber.e(error, "Socket message stream error"));
        socketDisposables.add(d);
    }

    public void stopObservingIncomingMessages() {
        socketDisposables.clear();
    }

    // ─────────────────────────────────────────────────────────────
    // FETCH HISTORY – API → Room (offset-based pagination)
    // ─────────────────────────────────────────────────────────────

    /**
     * Tải tin nhắn từ server (offset-based) rồi upsert vào Room.
     * UI chỉ observe Flowable từ Room, không cần xử lý kết quả trực tiếp.
     *
     * @param conversationId ID conversation
     * @param limit          Số lượng tin mỗi trang
     * @param offset         Vị trí bắt đầu lấy (= số item đang có trên UI)
     * @return Single&lt;Boolean&gt; – {@code true} nếu server trả về đủ
     * {@code limit}
     * item (có thể còn trang tiếp theo)
     */
    public Single<Boolean> fetchChatHistory(String conversationId, int limit, int offset) {
        return apiService.fetchChatHistory(conversationId, limit, offset)
                .subscribeOn(Schedulers.io())
                .flatMap(response -> {
                    if (response == null || response.getData() == null) {
                        return Single.just(false);
                    }

                    List<MessageDto> dtos = response.getData().messages;

                    if (dtos == null || dtos.isEmpty()) {
                        return Single.just(false);
                    }

                    // hasMore: server trả đủ limit → còn trang kế tiếp
                    boolean hasMore = dtos.size() == limit;

                    // Map DTO → Entity
                    List<MessageEntity> entities = new ArrayList<>();
                    List<MessageAttachmentEntity> attachments = new ArrayList<>();

                    for (MessageDto dto : dtos) {
                        entities.add(mapDtoToEntity(dto));
                        if (dto.attachments != null) {
                            for (AttachmentDto att : dto.attachments) {
                                attachments.add(mapAttachmentDtoToEntity(att));
                            }
                        }
                    }

                    // Upsert toàn bộ vào Room, trả về hasMore
                    return chatDao.insertMessages(entities)
                            .andThen(attachments.isEmpty()
                                    ? Completable.complete()
                                    : chatDao.insertAttachments(attachments))
                            .andThen(Single.just(hasMore));
                });
    }

    // ─────────────────────────────────────────────────────────────
    // CLEANUP – dọn rác Room, giữ bảng nhẹ nhàng
    // ─────────────────────────────────────────────────────────────

    /**
     * Xóa tin nhắn cũ của một conversation, chỉ giữ lại
     * {@link #KEEP_MESSAGES_COUNT} tin mới nhất.
     * Nên gọi sau khi fetch history thành công hoặc khi user rời màn hình.
     */
    public Completable cleanupOldMessages(String conversationId) {
        return chatDao.deleteOldMessages(conversationId, KEEP_MESSAGES_COUNT)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> Timber.d("Cleanup done for conversation: %s (kept %d)", conversationId,
                        KEEP_MESSAGES_COUNT))
                .doOnError(e -> Timber.e(e, "Cleanup failed for conversation: %s", conversationId));
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE STATUS
    // ─────────────────────────────────────────────────────────────

    /**
     * Đánh dấu tất cả tin nhắn trong conversation là READ.
     * Gọi khi user mở màn hình chat.
     */
    public Completable markAllAsRead(String conversationId) {
        return chatDao.updateAllMessagesStatusInConversation(
                        conversationId,
                        MessageStatus.READ,
                        System.currentTimeMillis())
                .subscribeOn(Schedulers.io());
    }

    /**
     * Cập nhật trạng thái một tin nhắn cụ thể (từ socket event message:read, v.v.)
     */
    public Completable updateMessageStatus(String messageId, @MessageStatus String status) {
        return chatDao.updateMessageStatus(messageId, status, System.currentTimeMillis())
                .subscribeOn(Schedulers.io());
    }

    // ─────────────────────────────────────────────────────────────
    // MAPPER helpers (private)
    // ─────────────────────────────────────────────────────────────

    private MessageEntity mapSocketEventToEntity(SocketMessageEvent event) {
        return new MessageEntity(
                event.messageId,
                event.conversationId,
                event.senderId,
                event.content != null ? event.content : "",
                normalizeMessageType(event.messageType),
                normalizeMessageStatus(event.messageStatus),
                new Date(event.createdAt),
                new Date(event.updatedAt > 0 ? event.updatedAt : event.createdAt),
                null);
    }

    private MessageEntity mapDtoToEntity(MessageDto dto) {
        return new MessageEntity(
                dto.messageId,
                dto.conversationId,
                dto.senderId,
                dto.content != null ? dto.content : "",
                normalizeMessageType(dto.messageType),
                normalizeMessageStatus(dto.messageStatus),
                dto.createdAt != null ? dto.createdAt : new Date(),
                dto.updatedAt != null ? dto.updatedAt : new Date(),
                dto.deletedAt);
    }

    private MessageAttachmentEntity mapAttachmentDtoToEntity(AttachmentDto dto) {
        return new MessageAttachmentEntity(
                dto.attachmentId,
                dto.messageId,
                dto.fileName != null ? dto.fileName : "",
                dto.fileUrl != null ? dto.fileUrl : "",
                dto.fileType != null ? dto.fileType : "",
                dto.fileSize,
                dto.mimeType,
                dto.cloudFileId,
                dto.createdAt != null ? dto.createdAt : new Date());
    }

    /**
     * Chuẩn hoá messageType từ server về @StringDef hợp lệ
     */
    @MessageType
    private String normalizeMessageType(String raw) {
        if (raw == null)
            return MessageType.TEXT;
        switch (raw.toUpperCase()) {
            case "IMAGE":
                return MessageType.IMAGE;
            case "FILE":
                return MessageType.FILE;
            default:
                return MessageType.TEXT;
        }
    }

    /**
     * Chuẩn hoá messageStatus từ server về @StringDef hợp lệ
     */
    @MessageStatus
    private String normalizeMessageStatus(String raw) {
        if (raw == null)
            return MessageStatus.SENT;
        switch (raw.toUpperCase()) {
            case "DELIVERED":
                return MessageStatus.DELIVERED;
            case "READ":
                return MessageStatus.READ;
            default:
                return MessageStatus.SENT;
        }
    }
}
