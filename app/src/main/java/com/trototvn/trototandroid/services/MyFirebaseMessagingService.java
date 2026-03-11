package com.trototvn.trototandroid.services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.trototvn.trototandroid.data.local.dao.ChatDao;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.data.local.entity.MessageStatus;
import com.trototvn.trototandroid.data.local.entity.MessageType;
import com.trototvn.trototandroid.utils.NotificationHelper;
import com.trototvn.trototandroid.utils.SessionManager;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

@AndroidEntryPoint
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    SessionManager sessionManager;

    @Inject
    ChatDao chatDao;

    @Inject
    Gson gson;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Timber.d("New FCM Token: %s", token);
        sessionManager.saveFcmToken(token);
        // In the future, send this token to your backend if needed
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Timber.d("From: %s", remoteMessage.getFrom());

        NotificationHelper notificationHelper = new NotificationHelper(this);

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            notificationHelper.showNotification(title, body);
        }

        // Check if message contains a data payload.
        if (!remoteMessage.getData().isEmpty()) {
            Map<String, String> data = remoteMessage.getData();
            Timber.d("Message data payload: %s", data);

            // Logic Chat Sync (SSOT)
            if ("chat".equals(data.get("type"))) {
                handleChatMessage(data);
            }

            // If the message only has data, show a notification from data
            if (remoteMessage.getNotification() == null) {
                String title = data.get("title");
                String body = data.get("body");
                if (title != null && body != null) {
                    notificationHelper.showNotification(title, body);
                }
            }
        }
    }

    /**
     * Trích xuất payload chat, map sang MessageEntity và insert vào Room DB
     * (background thread)
     */
    private void handleChatMessage(Map<String, String> data) {
        try {
            String messageId = data.get("messageId");
            String conversationId = data.get("conversationId");
            String senderId = data.get("senderId");
            String content = data.get("content");
            String messageType = data.get("messageType");
            String messageStatus = data.get("messageStatus");
            String createdAtStr = data.get("createdAt");

            if (messageId == null || conversationId == null)
                return;

            long createdAt = createdAtStr != null ? Long.parseLong(createdAtStr) : System.currentTimeMillis();

            assert senderId != null;
            MessageEntity entity = new MessageEntity(
                    messageId,
                    conversationId,
                    senderId,
                    content != null ? content : "",
                    normalizeMessageType(messageType),
                    normalizeMessageStatus(messageStatus),
                    new Date(createdAt),
                    new Date(System.currentTimeMillis()),
                    null);

            // Insert trực tiếp vào Room DB trên luồng background
            chatDao.insertMessage(entity)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> Timber.d("FCM chat message synced to Room: %s", messageId),
                            err -> Timber.e(err, "FCM chat sync failed"));
        } catch (Exception e) {
            Timber.e(e, "Error parsing FCM chat payload");
        }
    }

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