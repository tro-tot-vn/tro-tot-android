package com.trototvn.trototandroid.data.local.entity;

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
    @ColumnInfo(name = "participant_id")
    public long participantId;

    @ColumnInfo(name = "conversation_id")
    public long conversationId;

    @ColumnInfo(name = "customer_id")
    public long customerId;

    public ConversationParticipantEntity(
            long participantId,
            long conversationId,
            long customerId) {
        this.participantId = participantId;
        this.conversationId = conversationId;
        this.customerId = customerId;
    }
}
