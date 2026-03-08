package com.trototvn.trototandroid.services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.trototvn.trototandroid.utils.NotificationHelper;
import com.trototvn.trototandroid.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    SessionManager sessionManager;

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
            Timber.d("Message data payload: %s", remoteMessage.getData());
            // If the message only has data, show a notification from data
            if (remoteMessage.getNotification() == null) {
                String title = remoteMessage.getData().get("title");
                String body = remoteMessage.getData().get("body");
                if (title != null && body != null) {
                    notificationHelper.showNotification(title, body);
                }
            }
        }
    }
}