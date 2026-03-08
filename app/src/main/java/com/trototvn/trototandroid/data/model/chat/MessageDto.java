package com.trototvn.trototandroid.data.model.chat;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * DTO - Tin nhắn trả về từ API /chat/history
 */
public class MessageDto {

    @SerializedName("messageId")
    public String messageId;

    @SerializedName("conversationId")
    public String conversationId;

    @SerializedName("senderId")
    public String senderId;

    @SerializedName("content")
    public String content;

    @SerializedName("messageType")
    public String messageType; // "TEXT" | "IMAGE" | "FILE"

    @SerializedName("messageStatus")
    public String messageStatus; // "SENT" | "DELIVERED" | "READ"

    @SerializedName("createdAt")
    public Date createdAt;

    @SerializedName("updatedAt")
    public Date updatedAt;

    @SerializedName("deletedAt")
    public Date deletedAt;

    @SerializedName("attachments")
    public List<AttachmentDto> attachments;
}
