package com.trototvn.trototandroid.data.local.entity;

import androidx.room.Embedded;
import androidx.room.ColumnInfo;

/**
 * UI Model using Room Projection to fetch conversation data along with 
 * the senderId and status of the latest message from the messages table.
 */
public class ConversationUIModel {
    @Embedded
    public ConversationEntity conversation;

    @ColumnInfo(name = "lastMessageSenderId")
    public Long lastMessageSenderId;

    @ColumnInfo(name = "lastMessageStatus")
    public String lastMessageStatus;
}
