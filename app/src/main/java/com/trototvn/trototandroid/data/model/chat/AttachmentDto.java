package com.trototvn.trototandroid.data.model.chat;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * DTO - File đính kèm trả về từ API
 */
public class AttachmentDto {

    @SerializedName("attachmentId")
    public long attachmentId;

    @SerializedName("messageId")
    public long messageId;

    @SerializedName("fileName")
    public String fileName;

    @SerializedName("fileUrl")
    public String fileUrl;

    @SerializedName("fileType")
    public String fileType;

    @SerializedName("fileSize")
    public Long fileSize;

    @SerializedName("mimeType")
    public String mimeType;

    @SerializedName("cloudFileId")
    public String cloudFileId;

    @SerializedName("createdAt")
    public Date createdAt;
}
