package com.trototvn.trototandroid.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "conversations")
public class ConversationEntity {

    @PrimaryKey
    @ColumnInfo(name = "conversation_id")
    public long conversationId;

    @ColumnInfo(name = "partner_name")
    public String partnerName;

    @ColumnInfo(name = "partner_avatar")
    public String partnerAvatar;

    @ColumnInfo(name = "last_message")
    public String lastMessage;

    @ColumnInfo(name = "unread_count")
    public int unreadCount;

    @NonNull
    @ColumnInfo(name = "created_at")
    public Date createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    public Date updatedAt;

    public ConversationEntity(
            long conversationId,
            String partnerName,
            String partnerAvatar,
            String lastMessage,
            int unreadCount,
            @NonNull Date createdAt,
            @NonNull Date updatedAt) {
        this.conversationId = conversationId;
        this.partnerName = partnerName;
        this.partnerAvatar = partnerAvatar;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
