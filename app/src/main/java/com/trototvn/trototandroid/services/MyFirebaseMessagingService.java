package com.trototvn.trototandroid.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.data.local.dao.ChatDao;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.data.local.entity.MessageStatus;
import com.trototvn.trototandroid.data.local.entity.MessageType;
import com.trototvn.trototandroid.ui.videocall.IncomingCallActivity;
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

    @Inject
    com.trototvn.trototandroid.data.repository.ChatRepository chatRepository;

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
        Map<String, String> data = remoteMessage.getData();
        String type = data.get("type");

        if ("VIDEO_CALL_REQUEST".equals(type)) {
            handleVideoCallRequest(data);
            return;
        } else if ("VIDEO_CALL_CANCELLED".equals(type)) {
            handleVideoCallCancelled(data);
            return;
        }

        // 1. Đồng bộ tin nhắn chat chạy ngầm
        if ("chat".equals(type)) {
            handleChatMessage(data);
        }

        // 2. Trích xuất tiêu đề & nội dung chuẩn hóa phù hợp từng loại thông báo (Type-aware Fallback)
        String title = null;
        String body = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        } else if (!data.isEmpty()) {
            if ("chat".equals(type)) {
                // Ưu tiên các trường đặc thù của tin nhắn Chat
                title = data.get("senderName");
                if (title == null) {
                    title = data.get("title");
                }

                body = data.get("messageContent");
                if (body == null) {
                    body = data.get("content");
                }
                if (body == null) {
                    body = data.get("body");
                }
            } else {
                // Các thông báo hệ thống/đăng ký thông thường
                title = data.get("title");
                body = data.get("body");
            }
        }

        // 3. Hiển thị duy nhất một lệnh thông qua Generic showNotification
        if (title != null && body != null) {
            notificationHelper.showNotification(title, body, data);
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

            if (messageIdStr == null || conversationIdStr == null || senderIdStr == null) {
                return;
            }

            long messageId = Long.parseLong(messageIdStr);
            long conversationId = Long.parseLong(conversationIdStr);
            long senderId = Long.parseLong(senderIdStr);

            // Phân tích ngày tháng theo chuẩn ISO 8601
            long createdAt = parseSafeDate(createdAtStr);

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

            chatRepository.saveIncomingMessageEntity(entity)
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
        if (raw == null) {
            return MessageType.TEXT;
        }
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

    @MessageStatus
    private String normalizeMessageStatus(String raw) {
        if (raw == null) {
            return MessageStatus.SENT;
        }
        switch (raw.toUpperCase()) {
            case "DELIVERED":
                return MessageStatus.DELIVERED;
            case "READ":
                return MessageStatus.READ;
            default:
                return MessageStatus.SENT;
        }
    }

    /**
     * Phân tích ngày tháng theo chuẩn ISO 8601 từ Server Node.js
     */
    private long parseSafeDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return System.currentTimeMillis();
        }
        String cleanStr = dateStr.trim();

        // 1. Phân tích định dạng chuẩn ISO 8601 (Khuyến nghị)
        try {
            java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
            java.util.Date parsedDate = isoFormat.parse(cleanStr);
            if (parsedDate != null) return parsedDate.getTime();
        } catch (Exception ignored) {
        }

        // 2. Fallback: Dạng Số nguyên (Unix timestamp) cho tương thích ngược
        try {
            return Long.parseLong(cleanStr);
        } catch (NumberFormatException ignored) {
        }

        return System.currentTimeMillis();
    }

    private void handleVideoCallRequest(Map<String, String> data) {
        String roomId = data.get("roomId");
        String callerIdStr = data.get("callerId");
        String callerName = data.get("callerName");

        if (roomId == null || callerIdStr == null) {
            Timber.w("Invalid VIDEO_CALL_REQUEST payload");
            return;
        }

        long callerId = 0;
        try {
            callerId = Long.parseLong(callerIdStr);
        } catch (NumberFormatException e) {
            Timber.e(e, "Invalid callerId format: %s", callerIdStr);
        }

        // Truy vấn nhanh Local Room DB để tìm avatar của callerId
        String callerAvatar = null;
        if (callerId > 0) {
            try {
                callerAvatar = chatDao.getPartnerAvatarByCustomerIdSync(callerId);
            } catch (Exception e) {
                Timber.e(e, "Error querying caller avatar");
            }
        }

        // Chuẩn bị Full-Screen Intent trỏ tới IncomingCallActivity
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("callerId", callerIdStr);
        intent.putExtra("callerName", callerName != null ? callerName : getString(R.string.notification_title_default));
        intent.putExtra("callerAvatar", callerAvatar);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                roomId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = "trotot_call_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        // Tạo Notification Channel ưu tiên cao kèm nhạc chuông
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    getString(R.string.call_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(getString(R.string.call_notification_channel_desc));
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            channel.setSound(ringtoneUri, new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        String displayName = callerName != null ? callerName : getString(R.string.notification_title_default);
        String contentText = getString(R.string.incoming_video_call_content, displayName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_phone)
                .setContentTitle(getString(R.string.incoming_video_call))
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSound(ringtoneUri)
                .setVibrate(new long[]{0, 1000, 500, 1000});

        if (notificationManager != null) {
            notificationManager.notify(roomId.hashCode(), builder.build());
        }
    }

    private void handleVideoCallCancelled(Map<String, String> data) {
        String roomId = data.get("roomId");
        if (roomId == null) {
            Timber.w("Invalid VIDEO_CALL_CANCELLED payload: missing roomId");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(roomId.hashCode());
        }

        // Phát Broadcast nội bộ ra hiệu đóng màn hình IncomingCallActivity lập tức
        Intent cancelIntent = new Intent(IncomingCallActivity.ACTION_VIDEO_CALL_CANCELLED);
        cancelIntent.putExtra("roomId", roomId);
        cancelIntent.setPackage(getPackageName());
        sendBroadcast(cancelIntent);

        Timber.d("Đã gửi broadcast hủy cuộc gọi cho phòng: %s", roomId);
    }
}