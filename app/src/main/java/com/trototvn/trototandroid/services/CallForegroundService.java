package com.trototvn.trototandroid.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.ui.videocall.IncomingCallActivity;

import timber.log.Timber;

/**
 * Service chạy ngầm (Foreground Service) dùng để duy trì cuộc gọi
 * khi người dùng quay lại màn hình chính hoặc chuyển sang ứng dụng khác.
 */
public class CallForegroundService extends Service {

    public static final String ACTION_START_CALL = "com.trototvn.trototandroid.ACTION_START_CALL";
    public static final String ACTION_STOP_CALL = "com.trototvn.trototandroid.ACTION_STOP_CALL";
    public static final String EXTRA_ROOM_ID = "room_id";
    public static final String EXTRA_PARTNER_NAME = "partner_name";

    private static final int NOTIFICATION_ID = 10002;
    private static final String CHANNEL_ID = "trotot_active_call_channel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START_CALL.equals(action)) {
                String roomId = intent.getStringExtra(EXTRA_ROOM_ID);
                String partnerName = intent.getStringExtra(EXTRA_PARTNER_NAME);
                Timber.d("Bắt đầu CallForegroundService cho phòng: %s, đối phương: %s", roomId, partnerName);
                startForegroundService(roomId, partnerName);
            } else if (ACTION_STOP_CALL.equals(action)) {
                Timber.d("Dừng CallForegroundService");
                stopForeground(true);
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    private void startForegroundService(String roomId, String partnerName) {
        // Tạo Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Cuộc gọi đang diễn ra",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Kênh hiển thị thông báo cuộc gọi đang diễn ra");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Intent quay lại màn hình cuộc gọi
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("callerName", partnerName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Cuộc gọi video")
                .setContentText("Đang đàm thoại với " + (partnerName != null ? partnerName : "đối phương"))
                .setSmallIcon(R.drawable.ic_phone)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        // Android 14+ yêu cầu chỉ định foregroundServiceType là phoneCall
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
