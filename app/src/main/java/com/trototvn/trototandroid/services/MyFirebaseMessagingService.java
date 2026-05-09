package com.trototvn.trototandroid.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.local.dao.ChatDao;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.data.local.entity.MessageStatus;
import com.trototvn.trototandroid.data.local.entity.MessageType;
import com.trototvn.trototandroid.ui.main.MainActivity;
import com.trototvn.trototandroid.utils.NotificationHelper;
import com.trototvn.trototandroid.utils.SessionManager;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
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

    @Inject
    com.trototvn.trototandroid.data.repository.AuthRepository authRepository;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Timber.d("New FCM Token: %s", token);
        sessionManager.saveFcmToken(token);

        if (sessionManager.isLoggedIn()) {
            disposables.add(
                    authRepository.registerFcmToken(token)
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> Timber.d("FCM Token synced successfully on new token"),
                                    error -> Timber.e(error, "Failed to sync FCM token")
                            )
            );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
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

                if (remoteMessage.getNotification() == null) {
                    String conversationIdStr = data.get("conversationId");
                    String senderName = data.get("senderName");
                    if (senderName == null)
                        senderName = data.get("title") != null ? data.get("title") : "Tin nhắn mới";
                    String content = data.get("messageContent");
                    if (content == null) content = data.get("content"); // Fallback to content field
                    if (content == null) content = data.get("body");

                    if (conversationIdStr != null && content != null) {
                        // Check if the user is currently in this conversation
                        if (conversationIdStr.equals(com.trototvn.trototandroid.App.activeConversationId)) {
                            Timber.d("User is in the chat detail, skipping notification");
                        } else {
                            sendNotification(senderName, content, conversationIdStr);
                        }
                    }
                }
            } else {
                // If the message only has data (and not chat type), show a generic notification
                if (remoteMessage.getNotification() == null) {
                    String title = data.get("title");
                    String body = data.get("body");
                    if (title != null && body != null) {
                        notificationHelper.showNotification(title, body);
                    }
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
            String messageIdStr = data.get("messageId");
            String conversationIdStr = data.get("conversationId");
            String senderIdStr = data.get("senderId");
            String content = data.get("content");
            String messageType = data.get("messageType");
            String messageStatus = data.get("messageStatus");
            String createdAtStr = data.get("createdAt");

            if (messageIdStr == null || conversationIdStr == null || senderIdStr == null)
                return;

            long messageId = Long.parseLong(messageIdStr);
            long conversationId = Long.parseLong(conversationIdStr);
            long senderId = Long.parseLong(senderIdStr);
            long createdAt = createdAtStr != null ? Long.parseLong(createdAtStr) : System.currentTimeMillis();

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

    private void sendNotification(String senderName, String messageBody, String conversationId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("conversationId", conversationId);
        intent.putExtra("partnerName", senderName);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        String channelId = "chat_channel_id";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Tin nhắn mới",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_trotot_logo_app)
                .setContentTitle(senderName)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        int notificationId = conversationId != null ? conversationId.hashCode() : (int) System.currentTimeMillis();

        try {
            NotificationManagerCompat.from(this).notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Timber.e(e, "No permission to show notification");
        }
    }
}