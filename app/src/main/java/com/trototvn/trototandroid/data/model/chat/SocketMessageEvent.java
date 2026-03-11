package com.trototvn.trototandroid.data.model.chat;

import com.google.gson.annotations.SerializedName;

/**
 * DTO - Socket event khi nhận tin nhắn mới (message:received)
 * Ánh xạ với backend SocketEvents.MESSAGE_RECEIVED payload
 */
public class SocketMessageEvent {

    @SerializedName("messageId")
    public long messageId;

    @SerializedName("conversationId")
    public long conversationId;

    @SerializedName("senderId")
    public long senderId;

    @SerializedName("content")
    public String content;

    @SerializedName("messageType")
    public String messageType;

    @SerializedName("messageStatus")
    public String messageStatus;

    @SerializedName("createdAt")
    public long createdAt; // epoch ms

    @SerializedName("updatedAt")
    public long updatedAt; // epoch ms
}
