package com.trototvn.trototandroid.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;

import com.trototvn.trototandroid.data.model.chat.AttachmentDto;

@Entity(tableName = "messages", indices = {
        @Index(value = "conversation_id"),
        @Index(value = "sender_id")
})
public class MessageEntity {

    @PrimaryKey
    @ColumnInfo(name = "message_id")
    public long messageId;

    @ColumnInfo(name = "conversation_id")
    public long conversationId;

    @ColumnInfo(name = "sender_id")
    public long senderId;

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

    @ColumnInfo(name = "attachments")
    private List<AttachmentDto> attachments;

    public List<AttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public MessageEntity(
            long messageId,
            long conversationId,
            long senderId,
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
