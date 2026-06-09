package com.trototvn.trototandroid.data.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.trototvn.trototandroid.data.local.dao.ChatDao;
import com.trototvn.trototandroid.data.local.entity.ConversationEntity;
import com.trototvn.trototandroid.data.local.entity.ConversationParticipantEntity;
import com.trototvn.trototandroid.data.local.entity.MessageAttachmentEntity;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.data.local.entity.MessageStatus;
import com.trototvn.trototandroid.data.local.entity.MessageType;
import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.chat.AttachmentDto;
import com.trototvn.trototandroid.data.model.chat.ConversationDto;
import com.trototvn.trototandroid.data.model.chat.MarkReadRequest;
import com.trototvn.trototandroid.data.model.chat.MessageDto;
import com.trototvn.trototandroid.data.model.chat.SendMessageRequest;
import com.trototvn.trototandroid.data.model.chat.SocketMessageEvent;
import com.trototvn.trototandroid.data.remote.ApiService;
import com.trototvn.trototandroid.utils.SessionManager;
import com.trototvn.trototandroid.utils.SocketIOManager;
import com.trototvn.trototandroid.utils.SocketEvents;

import android.content.Intent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import com.trototvn.trototandroid.utils.WebRtcManager;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

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
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

    public static class CallRoomInfo {
        public final String roomId;
        public final String partnerId;
        public final String partnerName;

        public CallRoomInfo(String roomId, String partnerId, String partnerName) {
            this.roomId = roomId;
            this.partnerId = partnerId;
            this.partnerName = partnerName;
        }
    }

    // Số tin nhắn tối đa giữ lại mỗi conversation khi cleanup
    private static final int KEEP_MESSAGES_COUNT = 100;

    private final Context context;
    private final ChatDao chatDao;
    private final ApiService apiService;
    private final SocketIOManager socketIOManager;
    private final SessionManager sessionManager;
    private final WebRtcManager webRtcManager;
    private final Gson gson;

    // Dùng CompositeDisposable để quản lý Socket subscription
    private final CompositeDisposable socketDisposables = new CompositeDisposable();

    private final io.socket.emitter.Emitter.Listener onIncomingCallRequestListener = this::onIncomingCallRequest;
    private final io.socket.emitter.Emitter.Listener onIncomingCallEndedListener = this::onIncomingCallEnded;

    private final java.util.concurrent.ConcurrentHashMap<String, String> activeHandshakes = new java.util.concurrent.ConcurrentHashMap<>();
    private final PublishSubject<Resource<CallRoomInfo>> callRoomSubject = PublishSubject.create();

    private void onRoomCreated(Object[] args) {
        Timber.d("onRoomCreated received in ChatRepository");
        if (args == null || args.length == 0 || args[0] == null) {
            Timber.w("onRoomCreated: Received empty or null arguments");
            callRoomSubject.onNext(Resource.error("Lỗi tạo phòng gọi từ máy chủ (dữ liệu trống)", null));
            return;
        }
        try {
            String rawPayload = args[0].toString();
            Timber.d("onRoomCreated raw payload: %s", rawPayload);
            JsonObject envelope = gson.fromJson(rawPayload, JsonObject.class);
            if (envelope.has("data") && !envelope.get("data").isJsonNull()) {
                JsonObject data = envelope.getAsJsonObject("data");
                if (data.has("roomId") && !data.get("roomId").isJsonNull() &&
                        data.has("calleeId") && !data.get("calleeId").isJsonNull()) {
                    String roomId = data.get("roomId").getAsString();
                    String calleeId = data.get("calleeId").getAsString();
                    String partnerName = activeHandshakes.remove(calleeId);
                    if (partnerName == null) {
                        partnerName = "Người dùng";
                    }
                    callRoomSubject.onNext(Resource.success(new CallRoomInfo(roomId, calleeId, partnerName)));
                } else {
                    callRoomSubject.onNext(Resource.error("Lỗi máy chủ: Dữ liệu phòng thiếu thông tin | Payload: " + rawPayload, null));
                }
            } else {
                String errorMsg = envelope.has("message") ? envelope.get("message").getAsString() : "Lỗi không xác định";
                callRoomSubject.onNext(Resource.error("Lỗi máy chủ: " + errorMsg + " | Payload: " + rawPayload, null));
            }
        } catch (Exception e) {
            Timber.e(e, "Error parsing roomCreated payload");
            callRoomSubject.onNext(Resource.error("Lỗi phân tích phản hồi từ máy chủ cuộc gọi | Payload: " + args[0].toString(), null));
        }
    }

    @Inject
    public ChatRepository(
            @ApplicationContext Context context,
            ChatDao chatDao,
            ApiService apiService,
            SocketIOManager socketIOManager,
            SessionManager sessionManager,
            WebRtcManager webRtcManager,
            Gson gson) {
        this.context = context;
        this.chatDao = chatDao;
        this.apiService = apiService;
        this.socketIOManager = socketIOManager;
        this.sessionManager = sessionManager;
        this.webRtcManager = webRtcManager;
        this.gson = gson;
    }

    public void observeIncomingCalls() {
        socketIOManager.off(SocketEvents.LISTEN_REQUEST, onIncomingCallRequestListener);
        socketIOManager.on(SocketEvents.LISTEN_REQUEST, onIncomingCallRequestListener);

        socketIOManager.off(SocketEvents.LISTEN_ENDED, onIncomingCallEndedListener);
        socketIOManager.on(SocketEvents.LISTEN_ENDED, onIncomingCallEndedListener);
    }

    public void stopObservingIncomingCalls() {
        socketIOManager.off(SocketEvents.LISTEN_REQUEST, onIncomingCallRequestListener);
        socketIOManager.off(SocketEvents.LISTEN_ENDED, onIncomingCallEndedListener);
    }

    private void onIncomingCallRequest(Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) return;
        try {
            JsonObject envelope = gson.fromJson(args[0].toString(), JsonObject.class);
            JsonObject data = envelope.has("data") && !envelope.get("data").isJsonNull()
                    ? envelope.getAsJsonObject("data")
                    : envelope;
            
            if (!data.has("roomId") || data.get("roomId").isJsonNull() ||
                    !data.has("callerId") || data.get("callerId").isJsonNull()) {
                Timber.w("onIncomingCallRequest: roomId or callerId is null or missing");
                return;
            }
            
            String roomId = data.get("roomId").getAsString();
            long callerId = data.get("callerId").getAsLong();

            // Check busy state: if user is already in a peer connection, auto-reject
            if (webRtcManager.getPeerConnection() != null) {
                Timber.d("User is busy, auto-rejecting incoming call: %s", roomId);
                JsonObject payload = new JsonObject();
                payload.addProperty("roomId", roomId);
                payload.addProperty("reason", "Busy");
                socketIOManager.emit(SocketEvents.EMIT_REJECTED, payload);
                return;
            }

            Disposable d = Single.fromCallable(() -> {
                String name = chatDao.getPartnerNameByCustomerIdSync(callerId);
                String avatar = chatDao.getPartnerAvatarByCustomerIdSync(callerId);
                return new String[]{name, avatar};
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                result -> {
                    String name = result[0];
                    String avatar = result[1];
                    Intent intent = new Intent(context, com.trototvn.trototandroid.ui.videocall.IncomingCallActivity.class);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("callerId", String.valueOf(callerId));
                    intent.putExtra("callerName", name != null && !name.trim().isEmpty() ? name : "Người dùng");
                    intent.putExtra("callerAvatar", avatar);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                },
                throwable -> {
                    Timber.e(throwable, "Error querying DB for incoming call partner");
                    Intent intent = new Intent(context, com.trototvn.trototandroid.ui.videocall.IncomingCallActivity.class);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("callerId", String.valueOf(callerId));
                    intent.putExtra("callerName", "Người dùng");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }
            );
            socketDisposables.add(d);
        } catch (Exception e) {
            Timber.e(e, "Error processing incoming call request");
        }
    }

    private void onIncomingCallEnded(Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) return;
        try {
            JsonObject envelope = gson.fromJson(args[0].toString(), JsonObject.class);
            JsonObject data = envelope.has("data") && !envelope.get("data").isJsonNull()
                    ? envelope.getAsJsonObject("data")
                    : envelope;
            
            if (!data.has("roomId") || data.get("roomId").isJsonNull()) {
                Timber.w("onIncomingCallEnded: roomId is null or missing");
                return;
            }
            
            String roomId = data.get("roomId").getAsString();

            Intent cancelIntent = new Intent(com.trototvn.trototandroid.ui.videocall.IncomingCallActivity.ACTION_VIDEO_CALL_CANCELLED);
            cancelIntent.putExtra("roomId", roomId);
            cancelIntent.setPackage(context.getPackageName());
            context.sendBroadcast(cancelIntent);
            Timber.d("Sent local broadcast ACTION_VIDEO_CALL_CANCELLED for roomId: %s", roomId);
        } catch (Exception e) {
            Timber.e(e, "Error processing incoming call ended");
        }
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
    public Flowable<List<com.trototvn.trototandroid.data.local.entity.ConversationUIModel>> observeConversations() {
        return chatDao.getConversationsWithStatus()
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
                .andThen(chatDao.updateConversationLastMessage(conversationId, content, now))
                .subscribeOn(Schedulers.io())
                .andThen(apiService.sendMessage(conversationId, new SendMessageRequest(conversationId, content, MessageType.TEXT))
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
                })
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable, "API Error on sendTextMessage");
                    return chatDao.updateMessageStatus(optimisticId, MessageStatus.ERROR, System.currentTimeMillis());
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
                .andThen(chatDao.updateConversationLastMessage(conversationId, MessageType.IMAGE.equals(messageType) ? "[Hình ảnh]" : (content != null && !content.isEmpty() ? content : "[Tập tin đính kèm]"), now))
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
        long optimisticId = -System.currentTimeMillis();
        long senderId = 0;
        try {
            if (sessionManager.getUserId() != null)
                senderId = Long.parseLong(sessionManager.getUserId());
        } catch (NumberFormatException ignored) {
        }

        Date now = new Date();
        MessageEntity optimistic = new MessageEntity(
                optimisticId, conversationId, senderId, caption != null ? caption : "",
                MessageType.IMAGE, MessageStatus.SENT, now, now, null);

        return chatDao.insertMessage(optimistic)
                .andThen(chatDao.updateConversationLastMessage(conversationId, "[Hình ảnh]", now))
                .subscribeOn(Schedulers.io())
                .andThen(Single.fromCallable(() -> {
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
                    String mimeType = java.net.URLConnection.guessContentTypeFromName(tempFile.getName());
                    if (mimeType == null) {
                        mimeType = tempFile.getName().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                    }
                    RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), tempFile);
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
                                            .andThen(chatDao.softDeleteMessage(optimisticId, now.getTime(), now.getTime()))
                                            .andThen(attachments.isEmpty() ? Completable.complete() : chatDao.insertAttachments(attachments))
                                            .andThen(Single.just(entity));
                                } else {
                                    return Single.error(new Exception("Upload failed: No data"));
                                }
                            }).onErrorResumeNext(throwable -> {
                                if (tempFile.exists()) tempFile.delete();
                                return Single.error(throwable);
                            });
                }))
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable, "API Error on uploadMediaAndSendMessage");
                    return chatDao.updateMessageStatus(optimisticId, MessageStatus.ERROR, System.currentTimeMillis())
                            .andThen(Single.error(throwable));
                })
                .subscribeOn(Schedulers.io());
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
        Disposable dReceived = socketIOManager.getMessageReceived()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        raw -> saveIncomingMessage(raw.toString()),
                        error -> Timber.e(error, "Socket message stream error"));
        socketDisposables.add(dReceived);

        Disposable dRead = socketIOManager.getMessageRead()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        raw -> handleMessageReadEvent(raw.toString()),
                        error -> Timber.e(error, "Socket message read stream error"));
        socketDisposables.add(dRead);
    }

    public void joinConversation(long conversationId) {
        socketIOManager.joinConversation(conversationId);
    }

    public void leaveConversation(long conversationId) {
        socketIOManager.leaveConversation(conversationId);
    }

    /**
     * Xử lý sự kiện message:read từ Socket.IO và cập nhật trạng thái trong database thành READ
     */
    public void handleMessageReadEvent(String jsonPayload) {
        Timber.d("handleMessageReadEvent: raw payload = %s", jsonPayload);
        if (jsonPayload == null || jsonPayload.trim().isEmpty()) return;

        try {
            com.google.gson.JsonElement element = gson.fromJson(jsonPayload, com.google.gson.JsonElement.class);
            List<Long> readIds = new ArrayList<>();

            if (element.isJsonObject()) {
                com.google.gson.JsonObject obj = element.getAsJsonObject();
                // 1. Trường hợp data envelope của Socket.IO chứa "data" key
                if (obj.has("data") && !obj.get("data").isJsonNull()) {
                    com.google.gson.JsonElement dataElement = obj.get("data");
                    if (dataElement.isJsonObject()) {
                        com.google.gson.JsonObject dataObj = dataElement.getAsJsonObject();
                        extractIdsFromJsonObject(dataObj, readIds);
                    } else if (dataElement.isJsonArray()) {
                        extractIdsFromJsonArray(dataElement.getAsJsonArray(), readIds);
                    } else if (dataElement.isJsonPrimitive()) {
                        try {
                            readIds.add(dataElement.getAsLong());
                        } catch (Exception ignored) {}
                    }
                } else {
                    // 2. Trường hợp Object trực tiếp (không nằm trong envelope "data")
                    extractIdsFromJsonObject(obj, readIds);
                }
            } else if (element.isJsonArray()) {
                // 3. Trường hợp payload là mảng ID trực tiếp
                extractIdsFromJsonArray(element.getAsJsonArray(), readIds);
            } else if (element.isJsonPrimitive()) {
                // 4. Trường hợp payload là ID đơn lẻ dạng số/chuỗi
                try {
                    readIds.add(element.getAsLong());
                } catch (Exception ignored) {}
            }

            if (!readIds.isEmpty()) {
                List<Completable> updates = new ArrayList<>();
                for (Long id : readIds) {
                    updates.add(chatDao.updateMessageStatus(id, MessageStatus.READ, System.currentTimeMillis()));
                }
                Disposable d = Completable.merge(updates)
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> Timber.d("Successfully marked messages %s as READ from socket event", readIds),
                                err -> Timber.e(err, "Failed to update message statuses for message:read socket event")
                        );
                socketDisposables.add(d);
            } else {
                Timber.w("No message IDs extracted from message:read payload: %s", jsonPayload);
            }
        } catch (Exception e) {
            Timber.e(e, "Error parsing message:read socket payload: %s", jsonPayload);
        }
    }

    private void extractIdsFromJsonObject(com.google.gson.JsonObject obj, List<Long> readIds) {
        if (obj.has("messageIds") && !obj.get("messageIds").isJsonNull()) {
            com.google.gson.JsonElement idsElement = obj.get("messageIds");
            if (idsElement.isJsonArray()) {
                extractIdsFromJsonArray(idsElement.getAsJsonArray(), readIds);
            }
        } else if (obj.has("messageId") && !obj.get("messageId").isJsonNull()) {
            try {
                readIds.add(obj.get("messageId").getAsLong());
            } catch (Exception ignored) {}
        }
    }

    private void extractIdsFromJsonArray(com.google.gson.JsonArray array, List<Long> readIds) {
        for (com.google.gson.JsonElement item : array) {
            if (item.isJsonPrimitive()) {
                try {
                    readIds.add(item.getAsLong());
                } catch (Exception ignored) {}
            } else if (item.isJsonObject()) {
                com.google.gson.JsonObject itemObj = item.getAsJsonObject();
                if (itemObj.has("messageId") && !itemObj.get("messageId").isJsonNull()) {
                    try {
                        readIds.add(itemObj.get("messageId").getAsLong());
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    /**
     * Lưu tin nhắn nhận được từ Socket vào Room DB và cập nhật hội thoại.
     */
    public void saveIncomingMessage(String jsonPayload) {
        Single.fromCallable(() -> {
            SocketMessageEvent event = gson.fromJson(jsonPayload, SocketMessageEvent.class);
            return mapSocketEventToEntity(event);
        })
        .flatMapCompletable(entity -> {
            String content = entity.content;
            if (MessageType.IMAGE.equals(entity.messageType)) {
                content = "[Hình ảnh]";
            } else if (MessageType.FILE.equals(entity.messageType)) {
                content = "[Tập tin đính kèm]";
            }

            // TODO: Triển khai In-app Notification tại đây.
            // Nếu người dùng đang online nhưng KHÔNG ở trong phòng chat này (activeConversationId != entity.conversationId)
            // thì kích hoạt hiển thị một Custom In-app Banner/Popup thông báo tin nhắn mới trượt từ trên xuống.

            return chatDao.insertMessage(entity)
                    .andThen(chatDao.updateConversationLastMessage(entity.conversationId, content, entity.createdAt));
        })
        .subscribeOn(Schedulers.io())
        .subscribe(
                () -> Timber.d("Saved incoming message & updated conversation"),
                err -> Timber.e(err, "Failed to save incoming message: %s", err.getMessage())
        );
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
    public Single<Boolean> fetchChatHistory(long conversationId, int limit, int offset) {
        return apiService.fetchChatHistory(conversationId, limit, offset)
                .subscribeOn(Schedulers.io())
                .flatMap(response -> {
                    if (response == null || response.getData() == null) {
                        return Single.just(false);
                    }

                    List<MessageDto> dtos = response.getData();

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
                    List<ConversationParticipantEntity> participantEntities = new ArrayList<>();
                    long currentUserId = 0;
                    try {
                        if (sessionManager.getUserId() != null)
                            currentUserId = Long.parseLong(sessionManager.getUserId());
                    } catch (Exception ignored) {
                    }

                    java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                    isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));

                    for (ConversationDto dto : response.getData()) {
                        Timber.d("ConversationDto JSON: %s", gson.toJson(dto));
                        String partnerName = "Người dùng ẩn danh";
                        String partnerAvatar = null;

                        if (dto.participants != null) {
                            for (com.trototvn.trototandroid.data.model.chat.ParticipantDto p : dto.participants) {
                                // Lưu participant entity cho database
                                participantEntities.add(new ConversationParticipantEntity(
                                        p.getSignalingId(),
                                        dto.conversationId,
                                        p.customerId
                                ));

                                if (p.customerId != currentUserId) {
                                    partnerName = (p.firstName != null ? p.firstName : "") + " " + (p.lastName != null ? p.lastName : "");
                                    partnerName = partnerName.trim();
                                    if (partnerName.isEmpty()) {
                                        partnerName = "Người dùng ẩn danh";
                                    }
                                    if (p.avatarId != null) {
                                        String baseUrl = com.trototvn.trototandroid.utils.Constants.BASE_URL;
                                        if (baseUrl.endsWith("/")) {
                                            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                                        }
                                        partnerAvatar = baseUrl + "/api/files/" + p.avatarId;
                                    }
                                }
                            }
                        }

                        Date lastDate = dto.updatedAt != null ? dto.updatedAt : new Date();
                        if (dto.getLastMessageAt() != null && !dto.getLastMessageAt().isEmpty()) {
                            try {
                                lastDate = isoFormat.parse(dto.getLastMessageAt());
                            } catch (Exception e) {
                                Timber.e(e, "Error parsing lastMessageAt");
                            }
                        }

                        String lastMsg = dto.getLastMessage();
                        if (lastMsg == null || lastMsg.trim().isEmpty()) {
                            if (dto.getLastMessageAt() != null && !dto.getLastMessageAt().isEmpty()) {
                                lastMsg = "[Hình ảnh]";
                            } else {
                                lastMsg = "";
                            }
                        }

                        entities.add(new ConversationEntity(
                                dto.conversationId,
                                partnerName,
                                partnerAvatar,
                                lastMsg,
                                0, // unreadCount (UI field)
                                dto.createdAt != null ? dto.createdAt : new Date(),
                                lastDate));
                    }

                    return chatDao.insertConversations(entities)
                            .andThen(participantEntities.isEmpty() ? Completable.complete() : chatDao.insertParticipants(participantEntities));
                });
    }

    /**
     * Đồng bộ tin nhắn bị lỡ khi offline (Handshake Sync).
     */
    public void performHandshakeSync() {
        Single.fromCallable(() -> {
            java.util.Date latestDate = chatDao.getLatestMessageTimestampSync();
            if (latestDate != null) {
                java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
                return isoFormat.format(latestDate);
            }
            return "1970-01-01T00:00:00.000Z";
        })
        .flatMap(since -> apiService.syncMissedMessages(since, 100))
        .flatMapCompletable(response -> {
            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                return Completable.complete();
            }
            
            List<MessageDto> dtos = response.getData();
            List<MessageEntity> entities = new ArrayList<>();
            List<MessageAttachmentEntity> attachments = new ArrayList<>();
            List<Completable> updates = new ArrayList<>();

            for (MessageDto dto : dtos) {
                MessageEntity entity = mapDtoToEntity(dto);
                entities.add(entity);
                if (dto.attachments != null) {
                    for (AttachmentDto att : dto.attachments) {
                        attachments.add(mapAttachmentDtoToEntity(att));
                    }
                }
                
                String content = entity.content;
                if (MessageType.IMAGE.equals(entity.messageType)) {
                    content = "[Hình ảnh]";
                } else if (MessageType.FILE.equals(entity.messageType)) {
                    content = "[Tập tin đính kèm]";
                }
                
                updates.add(chatDao.updateConversationLastMessage(entity.conversationId, content, entity.createdAt));
            }
            
            return chatDao.insertMessages(entities)
                .andThen(attachments.isEmpty() ? Completable.complete() : chatDao.insertAttachments(attachments))
                .andThen(Completable.merge(updates));
        })
        .subscribeOn(Schedulers.io())
        .subscribe(
            () -> Timber.d("Handshake sync completed successfully"),
            error -> Timber.e(error, "Handshake sync failed")
        );
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

    /**
     * Clear all tables from local Room database.
     */
    public Completable clearAllData() {
        return chatDao.deleteAllMessages()
                .andThen(chatDao.deleteAllConversations())
                .andThen(chatDao.deleteAllParticipants())
                .andThen(chatDao.deleteAllAttachments())
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> Timber.d("Successfully cleared all local chat database tables"))
                .doOnError(e -> Timber.e(e, "Failed to clear local chat database tables"));
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
    public Completable markMessagesAsRead(long conversationId, List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty())
            return Completable.complete();

        // Emit qua socket để bên gửi nhận được sự kiện Đã xem thời gian thực lập tức
        if (socketIOManager.isConnected()) {
            socketIOManager.emitMessageRead(conversationId, messageIds);
        }

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

    /**
     * Gọi API xóa tin nhắn. Nếu thành công, xóa cứng khỏi Room Database.
     */
    public Completable deleteMessage(long messageId) {
        return Completable.create(emitter -> {
            apiService.deleteMessage(messageId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful() || response.code() == 204) {
                        chatDao.deleteMessageLocally(messageId)
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> emitter.onComplete(),
                                        emitter::onError
                                );
                    } else {
                        emitter.onError(new Exception("Lỗi xóa tin nhắn API: " + response.code()));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    emitter.onError(t);
                }
            });
        });
    }

    // ─────────────────────────────────────────────────────────────
    // MAPPER helpers (private)
    // ─────────────────────────────────────────────────────────────

    private java.util.Date parseSafeDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return new java.util.Date();
        }
        String cleanStr = dateStr.trim();

        // 1. Định dạng ISO 8601 kèm mili giây và Z (ví dụ: "2026-06-10T00:14:00.066Z")
        try {
            java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
            java.util.Date parsedDate = isoFormat.parse(cleanStr);
            if (parsedDate != null) return parsedDate;
        } catch (Exception ignored) {}

        // 2. Định dạng ISO 8601 không kèm mili giây (ví dụ: "2026-06-10T00:14:00Z" hoặc "2026-06-10T00:14:00")
        try {
            String formatStr = cleanStr.endsWith("Z") ? "yyyy-MM-dd'T'HH:mm:ss'Z'" : "yyyy-MM-dd'T'HH:mm:ss";
            java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat(formatStr, java.util.Locale.US);
            if (cleanStr.endsWith("Z")) {
                isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
            }
            java.util.Date parsedDate = isoFormat.parse(cleanStr);
            if (parsedDate != null) return parsedDate;
        } catch (Exception ignored) {}

        // 3. Fallback: Dạng Số nguyên (Unix timestamp)
        try {
            long epochTime = Long.parseLong(cleanStr);
            if (epochTime > 0) {
                // Nếu epochTime là giây (dưới 10 tỷ), đổi sang mili giây
                if (epochTime < 10000000000L) {
                    return new java.util.Date(epochTime * 1000);
                }
                return new java.util.Date(epochTime);
            }
        } catch (NumberFormatException ignored) {}

        return new java.util.Date();
    }

    private MessageEntity mapSocketEventToEntity(SocketMessageEvent event) {
        java.util.Date createdAt = event.createdAt != null ? event.createdAt : new java.util.Date();
        java.util.Date updatedAt = event.updatedAt != null ? event.updatedAt : createdAt;
        MessageEntity entity = new MessageEntity(
                event.messageId,
                event.conversationId,
                event.senderId,
                event.content != null ? event.content : "",
                normalizeMessageType(event.messageType),
                normalizeMessageStatus(event.messageStatus),
                createdAt,
                updatedAt,
                null);
        entity.setAttachments(event.attachments);
        return entity;
    }

    private MessageEntity mapDtoToEntity(MessageDto dto) {
        MessageEntity entity = new MessageEntity(
                dto.messageId,
                dto.conversationId,
                dto.senderId,
                dto.content != null ? dto.content : "",
                normalizeMessageType(dto.messageType),
                normalizeMessageStatus(dto.messageStatus),
                dto.createdAt != null ? dto.createdAt : new Date(),
                dto.updatedAt != null ? dto.updatedAt : new Date(),
                dto.deletedAt);
        entity.setAttachments(dto.attachments);
        return entity;
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
            case "CALL":
                return MessageType.CALL;
            default:
                return MessageType.TEXT;
        }
    }

    /**
     * Lấy thông tin cuộc hội thoại theo ID
     */
    public Single<ConversationEntity> getConversationById(long conversationId) {
        return chatDao.getConversationById(conversationId);
    }

    /**
     * Lắng nghe sự kiện phòng cuộc gọi được tạo từ Socket
     */
    public Observable<Resource<CallRoomInfo>> observeCallRoomCreated() {
        socketIOManager.off(SocketEvents.LISTEN_ROOM_CREATED);
        socketIOManager.on(SocketEvents.LISTEN_ROOM_CREATED, this::onRoomCreated);
        return callRoomSubject;
    }

    /**
     * Khởi tạo quá trình gọi: tìm Callee ID rồi bắn socket tạo phòng
     */
    public void startCallHandshake(long conversationId, String partnerName) {
        Timber.d("startCallHandshake: socket connected = %b", socketIOManager.isConnected());
        if (!socketIOManager.isConnected()) {
            callRoomSubject.onNext(Resource.error("Không có kết nối đến máy chủ cuộc gọi. Vui lòng kiểm tra lại mạng hoặc thử lại sau.", null));
            return;
        }
        Disposable d = getPartnerId(conversationId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        partnerId -> {
                            String calleeStr = String.valueOf(partnerId);
                            if (partnerName != null) {
                                activeHandshakes.put(calleeStr, partnerName);
                            }
                            JsonObject payload = new JsonObject();
                            payload.addProperty("calleeId", calleeStr);
                            socketIOManager.emit(SocketEvents.EMIT_CREATE_ROOM, payload);
                            Timber.d("Emitted video:call:createRoom for partnerId: %d, socket connected: %b", partnerId, socketIOManager.isConnected());
                        },
                        error -> {
                            Timber.e(error, "Failed to get partner ID to start call");
                            callRoomSubject.onNext(Resource.error("Không tìm thấy đối phương trong cuộc hội thoại", null));
                        }
                );
        socketDisposables.add(d);
    }

    /**
     * Lấy user ID của đối phương trong cuộc hội thoại
     */
    public Single<Long> getPartnerId(long conversationId) {
        long currentUserId = 0;
        try {
            String uid = sessionManager.getUserId();
            if (uid != null) {
                currentUserId = Long.parseLong(uid);
            }
        } catch (NumberFormatException ignored) {}

        final long myId = currentUserId;
        return chatDao.getParticipantsByConversationId(conversationId)
                .map(participants -> {
                    for (ConversationParticipantEntity p : participants) {
                        if (p.customerId != myId) {
                            return p.customerId;
                        }
                    }
                    throw new Exception("Partner not found");
                });
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
