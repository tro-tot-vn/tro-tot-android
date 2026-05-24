package com.trototvn.trototandroid.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "message_attachments", indices = {
        @Index(value = "message_id")
}, foreignKeys = @ForeignKey(entity = MessageEntity.class, parentColumns = "message_id", childColumns = "message_id", onDelete = ForeignKey.CASCADE))
public class MessageAttachmentEntity {

    @PrimaryKey
    @ColumnInfo(name = "attachment_id")
    public long attachmentId;

    @ColumnInfo(name = "message_id")
    public long messageId;

    @NonNull
    @ColumnInfo(name = "file_name")
    public String fileName;

    @NonNull
    @ColumnInfo(name = "file_url")
    public String fileUrl;

    @NonNull
    @ColumnInfo(name = "file_type")
    public String fileType;

    @Nullable
    @ColumnInfo(name = "file_size")
    public Long fileSize;

    @Nullable
    @ColumnInfo(name = "mime_type")
    public String mimeType;

    @Nullable
    @ColumnInfo(name = "cloud_file_id")
    public String cloudFileId;

    @NonNull
    @ColumnInfo(name = "created_at")
    public Date createdAt;

    public MessageAttachmentEntity(
            long attachmentId,
            long messageId,
            @NonNull String fileName,
            @NonNull String fileUrl,
            @NonNull String fileType,
            @Nullable Long fileSize,
            @Nullable String mimeType,
            @Nullable String cloudFileId,
            @NonNull Date createdAt) {
        this.attachmentId = attachmentId;
        this.messageId = messageId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.cloudFileId = cloudFileId;
        this.createdAt = createdAt;
    }
}
