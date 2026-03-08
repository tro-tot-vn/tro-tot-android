package com.trototvn.trototandroid.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "conversation_participants", indices = {
        @Index(value = "conversation_id"),
        @Index(value = "customer_id")
}, foreignKeys = @ForeignKey(entity = ConversationEntity.class, parentColumns = "conversation_id", childColumns = "conversation_id", onDelete = ForeignKey.CASCADE))
public class ConversationParticipantEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "participant_id")
    public String participantId;

    @NonNull
    @ColumnInfo(name = "conversation_id")
    public String conversationId;

    @NonNull
    @ColumnInfo(name = "customer_id")
    public String customerId;

    public ConversationParticipantEntity(
            @NonNull String participantId,
            @NonNull String conversationId,
            @NonNull String customerId) {
        this.participantId = participantId;
        this.conversationId = conversationId;
        this.customerId = customerId;
    }
}
