package com.trototvn.trototandroid.data.model.chat;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * DTO - Page dữ liệu trả về từ API phân trang chat history (offset-based)
 */
public class ChatHistoryResponse {

    @SerializedName("messages")
    public List<MessageDto> messages;

    /** Tổng số tin nhắn trong conversation (dùng để tính hasMore phía client). */
    @SerializedName("totalCount")
    public int totalCount;
}
