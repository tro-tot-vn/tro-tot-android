package com.trototvn.trototandroid.data.model.chat;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * DTO đại diện cho một cuộc hội thoại từ Backend
 */
public class ConversationDto {

    @SerializedName("conversationId")
    public long conversationId;

    @SerializedName("createdAt")
    public Date createdAt;

    @SerializedName("updatedAt")
    public Date updatedAt;

    @SerializedName("participants")
    public java.util.List<ParticipantDto> participants;

    @SerializedName("lastMessage")
    private String lastMessage;

    @SerializedName("lastMessageAt")
    private String lastMessageAt;

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(String lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
