package com.trototvn.trototandroid.utils;

/**
 * Constants for Socket.IO event names
 */
public class SocketEvents {
    // Connection events
    public static final String CONNECTION = "connection";
    public static final String DISCONNECT = "disconnect";
    public static final String ERROR = "error";

    // Message events
    public static final String MESSAGE_SENT = "message:sent";
    public static final String MESSAGE_RECEIVED = "message:received";
    public static final String MESSAGE_READ = "message:read";

    // Typing events
    public static final String TYPING_START = "typing:start";
    public static final String TYPING_STOP = "typing:stop";

    // Conversation events
    public static final String JOIN_CONVERSATION = "join:conversation";
    public static final String LEAVE_CONVERSATION = "leave:conversation";
    public static final String PARTICIPANT_JOINED = "participant:joined";
    public static final String PARTICIPANT_LEFT = "participant:left";

    // User status events
    public static final String USER_ONLINE = "user:online";
    public static final String USER_OFFLINE = "user:offline";

    // File events
    public static final String FILE_UPLOAD = "file:upload";
    public static final String FILE_UPLOADED = "file:uploaded";
    public static final String FILE_SENT = "file:sent";
    public static final String FILE_RECEIVED = "file:received";

    private SocketEvents() {
    }
}
