package com.trototvn.trototandroid.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "conversations")
public class ConversationEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "conversation_id")
    public String conversationId;

    @NonNull
    @ColumnInfo(name = "created_at")
    public Date createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    public Date updatedAt;

    public ConversationEntity(
            @NonNull String conversationId,
            @NonNull Date createdAt,
            @NonNull Date updatedAt) {
        this.conversationId = conversationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
