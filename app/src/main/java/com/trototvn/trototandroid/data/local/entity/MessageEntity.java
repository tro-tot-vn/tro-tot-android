package com.trototvn.trototandroid.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "messages", indices = {
        @Index(value = "conversation_id"),
        @Index(value = "sender_id")
})
public class MessageEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "message_id")
    public String messageId;

    @NonNull
    @ColumnInfo(name = "conversation_id")
    public String conversationId;

    @NonNull
    @ColumnInfo(name = "sender_id")
    public String senderId;

    @NonNull
    @ColumnInfo(name = "content")
    public String content;

    @NonNull
    @MessageType
    @ColumnInfo(name = "message_type")
    public String messageType;

    @NonNull
    @MessageStatus
    @ColumnInfo(name = "message_status")
    public String messageStatus;

    @NonNull
    @ColumnInfo(name = "created_at")
    public Date createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    public Date updatedAt;

    @Nullable
    @ColumnInfo(name = "deleted_at")
    public Date deletedAt;

    public MessageEntity(
            @NonNull String messageId,
            @NonNull String conversationId,
            @NonNull String senderId,
            @NonNull String content,
            @NonNull @MessageType String messageType,
            @NonNull @MessageStatus String messageStatus,
            @NonNull Date createdAt,
            @NonNull Date updatedAt,
            @Nullable Date deletedAt) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
        this.messageStatus = messageStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }
}
