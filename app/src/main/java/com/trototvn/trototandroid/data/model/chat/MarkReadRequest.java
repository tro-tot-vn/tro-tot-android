package com.trototvn.trototandroid.data.model.chat;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Yêu cầu đánh dấu danh sách tin nhắn là đã đọc
 */
public class MarkReadRequest {

    @SerializedName("messageIds")
    public List<Long> messageIds;

    public MarkReadRequest(List<Long> messageIds) {
        this.messageIds = messageIds;
    }
}
