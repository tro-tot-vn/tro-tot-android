package com.trototvn.trototandroid.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.trototvn.trototandroid.R;
import com.trototvn.trototandroid.ui.main.MainActivity;

import android.graphics.BitmapFactory;

/**
 * Helper class to manage and display notifications
 */
public class NotificationHelper {

    public static final String CHANNEL_ID = "trotot_notification_channel";
    public static final String CHAT_CHANNEL_ID = "trotot_chat_channel";
    private static final int NOTIFICATION_ID = 100;

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    /**
     * Create notification channel for Android 8.0+
     */
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_name);
            String description = context.getString(R.string.notification_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);

                NotificationChannel chatChannel = new NotificationChannel(CHAT_CHANNEL_ID, "Chat Messages", NotificationManager.IMPORTANCE_HIGH);
                chatChannel.setDescription("Notifications for new chat messages");
                notificationManager.createNotificationChannel(chatChannel);
            }
        }
    }

    /**
     * Show a simple text notification
     */
    public void showNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        @ColorInt
        int brandColor = ContextCompat.getColor(context, R.color.orange_500);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message) // Vector silhouette
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_trotot_logo_app))
                .setColor(brandColor)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    /**
     * Show a chat notification with high priority and deep-link intent
     */
    public void showChatNotification(String conversationId, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("conversationId", conversationId);
        intent.putExtra("partnerName", title);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) System.currentTimeMillis() % 10000, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        @ColorInt
        int brandColor = ContextCompat.getColor(context, R.color.orange_500);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHAT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_trotot_logo_app))
                .setColor(brandColor)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notifId = conversationId != null ? conversationId.hashCode() : (int) System.currentTimeMillis();
            notificationManager.notify(notifId, notificationBuilder.build());
        }
    }
}
