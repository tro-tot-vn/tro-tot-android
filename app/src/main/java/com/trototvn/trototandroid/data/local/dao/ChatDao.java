package com.trototvn.trototandroid.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.trototvn.trototandroid.data.local.entity.ConversationEntity;
import com.trototvn.trototandroid.data.local.entity.ConversationParticipantEntity;
import com.trototvn.trototandroid.data.local.entity.MessageAttachmentEntity;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;
import com.trototvn.trototandroid.data.local.entity.MessageStatus;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ChatDao {

    // ─────────────────────────────────────────────────────────────
    // INSERT – upsert (REPLACE on conflict)
    // ─────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertMessage(MessageEntity message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertMessages(List<MessageEntity> messages);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertConversation(ConversationEntity conversation);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertConversations(List<ConversationEntity> conversations);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertParticipant(ConversationParticipantEntity participant);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertParticipants(List<ConversationParticipantEntity> participants);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAttachment(MessageAttachmentEntity attachment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAttachments(List<MessageAttachmentEntity> attachments);

    // ─────────────────────────────────────────────────────────────
    // UPDATE message status
    // ─────────────────────────────────────────────────────────────

    @Query("UPDATE messages SET message_status = :status, updated_at = :updatedAt WHERE message_id = :messageId")
    Completable updateMessageStatus(
            long messageId,
            @MessageStatus String status,
            long updatedAt);

    @Query("UPDATE messages SET message_status = :status, updated_at = :updatedAt WHERE conversation_id = :conversationId AND message_status != :status")
    Completable updateAllMessagesStatusInConversation(
            long conversationId,
            @MessageStatus String status,
            long updatedAt);

    @Query("UPDATE messages SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE message_id = :messageId")
    Completable softDeleteMessage(long messageId, long deletedAt, long updatedAt);

    @Query("UPDATE conversations SET last_message = :lastMessage, updated_at = :updatedAt WHERE conversation_id = :conversationId")
    Completable updateConversationLastMessage(long conversationId, String lastMessage, java.util.Date updatedAt);

    // ─────────────────────────────────────────────────────────────
    // QUERY – observe (Flowable = real-time updates for UI)
    // ─────────────────────────────────────────────────────────────

    /**
     * Real-time stream of messages for a conversation, ordered newest-first.
     * Excludes soft-deleted messages (deleted_at IS NULL).
     * UI subscribes on IO, observes on Main.
     */
    @Query("SELECT * FROM messages WHERE conversation_id = :convId AND deleted_at IS NULL ORDER BY created_at ASC")
    Flowable<List<MessageEntity>> getMessagesByConversationId(long convId);

    @Query("SELECT * FROM conversations ORDER BY updated_at DESC")
    Flowable<List<ConversationEntity>> getAllConversations();

    @Query("SELECT c.conversation_id, c.partner_name, c.partner_avatar, c.last_message, " +
           "(SELECT COUNT(*) FROM messages m WHERE m.conversation_id = c.conversation_id AND m.sender_id != :currentUserId AND m.message_status = 'SENT' AND m.deleted_at IS NULL) AS unread_count, " +
           "c.created_at, c.updated_at, " +
           "(SELECT sender_id FROM messages m WHERE m.conversation_id = c.conversation_id ORDER BY created_at DESC LIMIT 1) AS lastMessageSenderId, " +
           "(SELECT message_status FROM messages m WHERE m.conversation_id = c.conversation_id ORDER BY created_at DESC LIMIT 1) AS lastMessageStatus " +
           "FROM conversations c ORDER BY c.updated_at DESC")
    Flowable<List<com.trototvn.trototandroid.data.local.entity.ConversationUIModel>> getConversationsWithStatus(long currentUserId);

    @Query("SELECT MAX(created_at) FROM messages")
    java.util.Date getLatestMessageTimestampSync();

    @Query("SELECT * FROM messages WHERE message_id = :messageId LIMIT 1")
    Single<MessageEntity> getMessageById(long messageId);

    @Query("SELECT * FROM message_attachments WHERE message_id = :messageId")
    Single<List<MessageAttachmentEntity>> getAttachmentsByMessageId(long messageId);

    @Query("SELECT * FROM conversation_participants WHERE conversation_id = :conversationId")
    Single<List<ConversationParticipantEntity>> getParticipantsByConversationId(long conversationId);

    // ─────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────

    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    Completable deleteMessagesByConversationId(long conversationId);

    @Query("DELETE FROM conversations WHERE conversation_id = :conversationId")
    Completable deleteConversation(long conversationId);

    @Query("DELETE FROM messages WHERE message_id = :messageId")
    Completable deleteMessageLocally(long messageId);

    @Query("DELETE FROM messages")
    Completable deleteAllMessages();

    @Query("DELETE FROM conversations")
    Completable deleteAllConversations();

    @Query("DELETE FROM conversation_participants")
    Completable deleteAllParticipants();

    @Query("DELETE FROM message_attachments")
    Completable deleteAllAttachments();

    // ─────────────────────────────────────────────────────────────
    // CLEANUP – giữ bảng Room gọn nhẹ
    // ─────────────────────────────────────────────────────────────

    /**
     * Xóa các tin nhắn cũ của một conversation, chỉ giữ lại :keepCount tin mới
     * nhất.
     * SQLite không có DELETE ... LIMIT trực tiếp nên dùng subquery.
     */
    @Query("DELETE FROM messages WHERE conversation_id = :conversationId AND message_id NOT IN (SELECT message_id FROM messages WHERE conversation_id = :conversationId ORDER BY created_at DESC LIMIT :keepCount)")
    Completable deleteOldMessages(long conversationId, int keepCount);

    @Query("SELECT * FROM conversations WHERE conversation_id = :conversationId LIMIT 1")
    Single<ConversationEntity> getConversationById(long conversationId);

    @Query("SELECT partner_avatar FROM conversations WHERE conversation_id IN (SELECT conversation_id FROM conversation_participants WHERE customer_id = :customerId) LIMIT 1")
    String getPartnerAvatarByCustomerIdSync(long customerId);

    @Query("SELECT partner_avatar FROM conversations WHERE conversation_id IN (SELECT conversation_id FROM conversation_participants WHERE participant_id = :participantId) LIMIT 1")
    String getPartnerAvatarByParticipantIdSync(long participantId);

    @Query("SELECT partner_name FROM conversations WHERE conversation_id IN (SELECT conversation_id FROM conversation_participants WHERE customer_id = :customerId) LIMIT 1")
    String getPartnerNameByCustomerIdSync(long customerId);
}
