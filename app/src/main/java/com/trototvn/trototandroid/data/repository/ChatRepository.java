package com.trototvn.trototandroid.data.repository;

import com.google.gson.Gson;
import com.trototvn.trototandroid.data.local.dao.ChatDao;
import com.trototvn.trototandroid.data.local.entity.ConversationEntity;
import com.trototvn.trototandroid.data.local.entity.MessageAttachmentEntity;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.data.local.entity.MessageStatus;
import com.trototvn.trototandroid.data.local.entity.MessageType;
import com.trototvn.trototandroid.data.model.chat.AttachmentDto;
import com.trototvn.trototandroid.data.model.chat.ConversationDto;
import com.trototvn.trototandroid.data.model.chat.MarkReadRequest;
import com.trototvn.trototandroid.data.model.chat.MessageDto;
import com.trototvn.trototandroid.data.model.chat.SendMessageRequest;
import com.trototvn.trototandroid.data.model.chat.SocketMessageEvent;
import com.trototvn.trototandroid.data.remote.ApiService;
import com.trototvn.trototandroid.utils.SessionManager;
import com.trototvn.trototandroid.utils.SocketIOManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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
    public Flowable<List<MessageEntity>> observeMessages(long conversationId) {
        return chatDao.getMessagesByConversationId(conversationId)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Trả về Flowable real-time danh sách hội thoại từ Room.
     * ViewModel subscribe trên IO, observe trên Main.
     */
    public Flowable<List<ConversationEntity>> observeConversations() {
        return chatDao.getAllConversations()
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
    public Completable sendTextMessage(long conversationId, String content) {
        long optimisticId = -System.currentTimeMillis();
        Date now = new Date();

        long senderId = 0;
        try {
            if (sessionManager.getUserId() != null)
                senderId = Long.parseLong(sessionManager.getUserId());
        } catch (NumberFormatException e) {
            Timber.e(e, "Invalid user ID format");
        }

        MessageEntity optimistic = new MessageEntity(
                optimisticId,
                conversationId,
                senderId,
                content,
                MessageType.TEXT,
                MessageStatus.SENT,
                now,
                now,
                null);

        return chatDao.insertMessage(optimistic)
                .subscribeOn(Schedulers.io())
                .andThen(apiService.sendMessage(conversationId, new SendMessageRequest(content, MessageType.TEXT))
                        .subscribeOn(Schedulers.io()))
                .flatMapCompletable(response -> {
                    if (response != null && response.getData() != null) {
                        MessageEntity serverMessage = mapDtoToEntity(response.getData());
                        // Upsert the real message from server (replaces optimistic if ID matches, or
                        // adds new)
                        return chatDao.insertMessage(serverMessage)
                                // Optional: Delete optimistic if server returns a different ID and we want to
                                // clean up
                                .andThen(chatDao.softDeleteMessage(optimisticId, now.getTime(), now.getTime()));
                    }
                    return Completable.complete();
                });
    }

    /**
     * Gửi tin nhắn kèm file (IMAGE / FILE):
     * 1. Insert MessageEntity (status SENT) vào Room
     * 2. Insert MessageAttachmentEntity vào Room
     * 3. Emit socket event FILE_SENT
     */
    public Completable sendFileMessage(
            long conversationId,
            String content,
            @MessageType String messageType,
            MessageAttachmentEntity attachment) {
        long optimisticId = -System.currentTimeMillis();
        Date now = new Date();

        long senderId = 0;
        try {
            if (sessionManager.getUserId() != null)
                senderId = Long.parseLong(sessionManager.getUserId());
        } catch (NumberFormatException e) {
            Timber.e(e, "Invalid user ID format");
        }

        MessageEntity message = new MessageEntity(
                optimisticId,
                conversationId,
                senderId,
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

    /**
     * Tải file lên và gửi tin nhắn (Atomic từ UI).
     */
    public Single<MessageEntity> uploadMediaAndSendMessage(long conversationId, Uri fileUri, String caption, Context context) {
        return Single.fromCallable(() -> {
            ContentResolver resolver = context.getContentResolver();
            InputStream inputStream = resolver.openInputStream(fileUri);
            if (inputStream == null) throw new IOException("Cannot open input stream");
            
            String mimeType = resolver.getType(fileUri);
            String extension = mimeType != null && mimeType.contains("/") ? mimeType.substring(mimeType.indexOf("/") + 1) : "tmp";
            
            File tempFile = new File(context.getCacheDir(), "upload_" + System.currentTimeMillis() + "." + extension);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            
            return tempFile;
        }).flatMap(tempFile -> {
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), tempFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);
            
            RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), caption != null ? caption : "");
            
            return apiService.sendFileMessage(conversationId, filePart, contentBody)
                    .flatMap(response -> {
                        if (tempFile.exists()) tempFile.delete();
                        
                        if (response != null && response.getData() != null) {
                            MessageDto dto = response.getData();
                            MessageEntity entity = mapDtoToEntity(dto);
                            
                            List<MessageAttachmentEntity> attachments = new ArrayList<>();
                            if (dto.attachments != null) {
                                for (AttachmentDto attDto : dto.attachments) {
                                    attachments.add(mapAttachmentDtoToEntity(attDto));
                                }
                            }
                            
                            return chatDao.insertMessage(entity)
                                    .andThen(attachments.isEmpty() ? Completable.complete() : chatDao.insertAttachments(attachments))
                                    .andThen(Single.just(entity));
                        } else {
                            return Single.error(new Exception("Upload failed: No data"));
                        }
                    }).doOnError(error -> {
                        if (tempFile.exists()) tempFile.delete();
                    });
        }).subscribeOn(Schedulers.io());
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
     *         {@code limit}
     *         item (có thể còn trang tiếp theo)
     */
    public Single<Boolean> fetchChatHistory(long conversationId, int limit, int offset) {
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

    /**
     * Tải danh sách hội thoại từ server rồi upsert vào Room (SSOT).
     */
    public Completable fetchConversations() {
        return apiService.fetchConversations()
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(response -> {
                    if (response == null || response.getData() == null || response.getData().isEmpty()) {
                        return Completable.complete();
                    }

                    List<ConversationEntity> entities = new ArrayList<>();
                    for (ConversationDto dto : response.getData()) {
                        entities.add(new ConversationEntity(
                                dto.conversationId,
                                "", // partnerName (UI field)
                                "", // lastMessage (UI field)
                                0, // unreadCount (UI field)
                                dto.createdAt != null ? dto.createdAt : new Date(),
                                dto.updatedAt != null ? dto.updatedAt : new Date()));
                    }

                    return chatDao.insertConversations(entities);
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
    public Completable cleanupOldMessages(long conversationId) {
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
    public Completable markAllAsRead(long conversationId) {
        return chatDao.updateAllMessagesStatusInConversation(
                conversationId,
                MessageStatus.READ,
                System.currentTimeMillis())
                .subscribeOn(Schedulers.io());
    }

    /**
     * Gọi API báo cáo server tin nhắn đã đọc, sau đó cập nhật local DB.
     */
    public Completable markMessagesAsRead(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty())
            return Completable.complete();

        return apiService.markAsRead(new MarkReadRequest(messageIds))
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(response -> {
                    List<Completable> updates = new ArrayList<>();
                    for (Long id : messageIds) {
                        updates.add(chatDao.updateMessageStatus(id, MessageStatus.READ, System.currentTimeMillis()));
                    }
                    return Completable.merge(updates);
                });
    }

    /**
     * Cập nhật trạng thái một tin nhắn cụ thể (từ socket event message:read, v.v.)
     */
    public Completable updateMessageStatus(long messageId, @MessageStatus String status) {
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
