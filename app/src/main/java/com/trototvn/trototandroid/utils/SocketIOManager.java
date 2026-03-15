package com.trototvn.trototandroid.utils;

import com.google.gson.Gson;

import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.socket.client.IO;
import io.socket.client.Socket;
import timber.log.Timber;

/**
 * SocketIOManager - Singleton manager for Socket.IO connections
 * Handles real-time chat events using RxJava subjects
 */
@Singleton
public class SocketIOManager {

    private final Gson gson;
    private Socket socket;

    private final PublishSubject<String> connectionStatusSubject = PublishSubject.create();
    private final PublishSubject<Object> messageReceivedSubject = PublishSubject.create();
    private final PublishSubject<Object> typingStatusSubject = PublishSubject.create();
    private final PublishSubject<Object> userStatusSubject = PublishSubject.create();

    @Inject
    public SocketIOManager(Gson gson) {
        this.gson = gson;
    }

    /**
     * Connect to Socket.IO server
     *
     * @param userId Current user ID for authentication
     */
    public void connect(String userId) {
        if (socket != null && socket.connected()) {
            return;
        }

        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnection = true;
            options.reconnectionDelay = 1000;
            options.reconnectionDelayMax = 5000;
            options.reconnectionAttempts = Integer.MAX_VALUE;
            options.timeout = 20000;

            // Pass userId in query as per backend implementation
            options.query = "userId=" + userId;

            // Also attempt handshake auth if supported by client version
            // options.auth = Collections.singletonMap("userId", userId);

            socket = IO.socket(Constants.BASE_URL, options);

            setupListeners();
            socket.connect();

            Timber.d("Starting Socket.IO connection for user: %s", userId);
        } catch (URISyntaxException e) {
            Timber.e(e, "Socket.IO URL error");
        }
    }

    private void setupListeners() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            Timber.i("Socket connected");
            connectionStatusSubject.onNext(SocketEvents.CONNECTION);
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            Timber.w("Socket disconnected");
            connectionStatusSubject.onNext(SocketEvents.DISCONNECT);
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            Timber.e("Socket connection error: %s", args[0]);
            connectionStatusSubject.onNext(SocketEvents.ERROR);
        });

        socket.on(SocketEvents.MESSAGE_RECEIVED, args -> {
            Timber.d("Socket message received: %s", args[0]);
            messageReceivedSubject.onNext(args[0]);
        });

        socket.on(SocketEvents.TYPING_START, args -> typingStatusSubject.onNext(args[0]));
        socket.on(SocketEvents.TYPING_STOP, args -> typingStatusSubject.onNext(args[0]));

        socket.on(SocketEvents.USER_ONLINE, args -> userStatusSubject.onNext(args[0]));
        socket.on(SocketEvents.USER_OFFLINE, args -> userStatusSubject.onNext(args[0]));

        socket.on("error", args -> Timber.e("Socket business error: %s", args[0]));
    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
            socket = null;
            Timber.d("Socket disconnected and cleared");
        }
    }

    /**
     * Emit join conversation event
     */
    public void joinConversation(long conversationId) {
        if (socket != null && socket.connected()) {
            socket.emit(SocketEvents.JOIN_CONVERSATION, conversationId);
            Timber.d("Joined conversation: %d", conversationId);
        }
    }

    /**
     * Emit leave conversation event
     */
    public void leaveConversation(long conversationId) {
        if (socket != null && socket.connected()) {
            socket.emit(SocketEvents.LEAVE_CONVERSATION, conversationId);
            Timber.d("Left conversation: %d", conversationId);
        }
    }

    /**
     * Emit message sent event
     */
    public void sendMessage(long conversationId, String content) {
        if (socket != null && socket.connected()) {
            // Create data structure as expected by backend (MessageSentEvent)
            // { conversationId, content, messageType }
            com.google.gson.JsonObject data = new com.google.gson.JsonObject();
            data.addProperty("conversationId", conversationId);
            data.addProperty("content", content);
            data.addProperty("messageType", "Text");

            socket.emit(SocketEvents.MESSAGE_SENT, data);
            Timber.d("Emitted message: %s to conversation: %d", content, conversationId);
        }
    }

    /**
     * Emit typing status
     */
    public void setTyping(long conversationId, boolean isTyping) {
        if (socket != null && socket.connected()) {
            com.google.gson.JsonObject data = new com.google.gson.JsonObject();
            data.addProperty("conversationId", conversationId);

            String event = isTyping ? SocketEvents.TYPING_START : SocketEvents.TYPING_STOP;
            socket.emit(event, data);
        }
    }

    // Observables for UI components
    public Observable<String> getConnectionStatus() {
        return connectionStatusSubject;
    }

    public Observable<Object> getMessageReceived() {
        return messageReceivedSubject;
    }

    public Observable<Object> getTypingStatus() {
        return typingStatusSubject;
    }

    public Observable<Object> getUserStatus() {
        return userStatusSubject;
    }

    public boolean isConnected() {
        return socket != null && socket.connected();
    }
}
