package com.trototvn.trototandroid.data.model.chat;

import com.google.gson.annotations.SerializedName;

/**
 * DTO dùng để gửi tin nhắn văn bản lên thẻ server
 */
public class SendMessageRequest {

    @SerializedName("content")
    public String content;

    @SerializedName("messageType")
    public String messageType;

    public SendMessageRequest(String content, String messageType) {
        this.content = content;
        this.messageType = messageType;
    }
}
