package com.trototvn.trototandroid.data.model.chat;

import com.google.gson.annotations.SerializedName;

/**
 * DTO dùng để gửi tin nhắn văn bản lên thẻ server
 */
public class SendMessageRequest {

    @SerializedName("conversationId")
    public long conversationId;

    @SerializedName("content")
    public String content;

    @SerializedName("messageType")
    public String messageType;

    public SendMessageRequest(long conversationId, String content, String messageType) {
        this.conversationId = conversationId;
        this.content = content;
        this.messageType = messageType;
    }
}
