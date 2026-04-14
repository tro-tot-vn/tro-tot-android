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
}
