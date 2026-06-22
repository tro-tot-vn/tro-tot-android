package com.trototvn.trototandroid.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.json.JSONObject;
import org.json.JSONArray;

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
    private final SessionManager sessionManager;
    private Socket socket;

    private final PublishSubject<String> connectionStatusSubject = PublishSubject.create();
    private final PublishSubject<Object> messageReceivedSubject = PublishSubject.create();
    private final PublishSubject<Object> messageReadSubject = PublishSubject.create();
    private final PublishSubject<Object> messageDeliveredSubject = PublishSubject.create();
    private final PublishSubject<Object> typingStatusSubject = PublishSubject.create();
    private final PublishSubject<Object> userStatusSubject = PublishSubject.create();

    @Inject
    public SocketIOManager(Gson gson, SessionManager sessionManager) {
        this.gson = gson;
        this.sessionManager = sessionManager;
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

            // Thêm Token vào quá trình xác thực Socket
            String token = sessionManager.getToken();
            String fcmToken = sessionManager.getFcmToken();
            if (token != null) {
                // Cách 1: Truyền qua Auth Map (Chuẩn Socket.IO v3/v4)
                java.util.Map<String, String> auth = new java.util.HashMap<>();
                auth.put("token", token);
                if (fcmToken != null) {
                    auth.put("x-fcm-token", fcmToken);
                }
                options.auth = auth;

                // Cách 2: Truyền qua Extra Headers (Nếu Backend yêu cầu Header)
                java.util.Map<String, java.util.List<String>> headers = new java.util.HashMap<>();
                headers.put("Authorization", java.util.Collections.singletonList("Bearer " + token));
                if (fcmToken != null) {
                    headers.put("x-fcm-token", java.util.Collections.singletonList(fcmToken));
                }
                options.extraHeaders = headers;
            }

            socket = IO.socket(Constants.BASE_URL, options);

            setupListeners();
            Timber.d("Connecting socket with token...");
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
            
            // Tự động rejoin conversation room khi kết nối lại nếu đang ở màn hình chat
            try {
                if (com.trototvn.trototandroid.App.activeConversationId != null) {
                    long activeId = Long.parseLong(com.trototvn.trototandroid.App.activeConversationId);
                    joinConversation(activeId);
                }
            } catch (Exception e) {
                Timber.e(e, "Error rejoining conversation room on socket connect");
            }
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

        socket.on(SocketEvents.FILE_RECEIVED, args -> {
            Timber.d("Socket file received: %s", args[0]);
            messageReceivedSubject.onNext(args[0]);
        });

        socket.on(SocketEvents.MESSAGE_READ, args -> {
            Timber.d("Socket message read: %s", args[0]);
            messageReadSubject.onNext(args[0]);
        });

        socket.on(SocketEvents.TYPING_START, args -> typingStatusSubject.onNext(args[0]));
        socket.on(SocketEvents.TYPING_STOP, args -> typingStatusSubject.onNext(args[0]));

        socket.on(SocketEvents.USER_ONLINE, args -> userStatusSubject.onNext(args[0]));
        socket.on(SocketEvents.USER_OFFLINE, args -> userStatusSubject.onNext(args[0]));

        socket.on(SocketEvents.MESSAGE_DELIVERED, args -> {
            Timber.d("Socket message delivered: %s", args[0]);
            messageDeliveredSubject.onNext(args[0]);
        });
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
     * Emit message read event
     */
    public void emitMessageRead(long conversationId, java.util.List<Long> messageIds) {
        if (socket != null && socket.connected()) {
            com.google.gson.JsonObject data = new com.google.gson.JsonObject();
            data.addProperty("conversationId", conversationId);
            
            com.google.gson.JsonArray idsArray = new com.google.gson.JsonArray();
            for (Long id : messageIds) {
                idsArray.add(id);
            }
            data.add("messageIds", idsArray);
            
            if (!messageIds.isEmpty()) {
                data.addProperty("messageId", messageIds.get(messageIds.size() - 1));
            }
            
            try {
                org.json.JSONObject payload = new org.json.JSONObject(data.toString());
                socket.emit(SocketEvents.MESSAGE_READ, payload);
                Timber.d("Emitted message:read via socket: %s", payload);
            } catch (Exception e) {
                Timber.e(e, "Error emitting message:read");
            }
        }
    }

    /**
    /**
     * Emit message delivered event
     */
    public void emitMessageDelivered(long conversationId, java.util.List<Long> messageIds) {
        if (socket != null && socket.connected()) {
            com.google.gson.JsonObject data = new com.google.gson.JsonObject();
            data.addProperty("conversationId", conversationId);
            
            com.google.gson.JsonArray idsArray = new com.google.gson.JsonArray();
            for (Long id : messageIds) {
                idsArray.add(id);
            }
            data.add("messageIds", idsArray);
            
            if (!messageIds.isEmpty()) {
                data.addProperty("messageId", messageIds.get(messageIds.size() - 1));
            }
            
            try {
                org.json.JSONObject payload = new org.json.JSONObject(data.toString());
                socket.emit(SocketEvents.MESSAGE_DELIVERED, payload);
                Timber.d("Emitted message:delivered via socket: %s", payload);
            } catch (Exception e) {
                Timber.e(e, "Error emitting message:delivered");
            }
        } else {
            Timber.w("emitMessageDelivered: Cannot emit message:delivered. Socket is %s", 
                    socket == null ? "NULL" : "DISCONNECTED");
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

    public Observable<Object> getMessageRead() {
        return messageReadSubject;
    }

    public Observable<Object> getMessageDelivered() {
        return messageDeliveredSubject;
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

    /**
     * Emit a generic Socket.IO event with payload
     */
    public void emit(String event, Object... args) {
        if (socket != null && socket.connected()) {
            if (args != null && args.length > 0) {
                Object[] processedArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof JsonObject) {
                        try {
                            processedArgs[i] = new JSONObject(args[i].toString());
                        } catch (Exception e) {
                            Timber.e(e, "Error converting GSON JsonObject to org.json.JSONObject");
                            processedArgs[i] = args[i];
                        }
                    } else if (args[i] instanceof JsonArray) {
                        try {
                            processedArgs[i] = new JSONArray(args[i].toString());
                        } catch (Exception e) {
                            Timber.e(e, "Error converting GSON JsonArray to org.json.JSONArray");
                            processedArgs[i] = args[i];
                        }
                    } else {
                        processedArgs[i] = args[i];
                    }
                }
                socket.emit(event, processedArgs);
            } else {
                socket.emit(event);
            }
        }
    }

    /**
     * Register a socket listener dynamically
     */
    public void on(String event, io.socket.emitter.Emitter.Listener listener) {
        if (socket != null) {
            socket.on(event, listener);
        }
    }

    /**
     * Unregister a socket listener dynamically
     */
    public void off(String event) {
        if (socket != null) {
            socket.off(event);
        }
    }

    /**
     * Unregister a specific socket listener dynamically
     */
    public void off(String event, io.socket.emitter.Emitter.Listener listener) {
        if (socket != null) {
            socket.off(event, listener);
        }
    }
}
