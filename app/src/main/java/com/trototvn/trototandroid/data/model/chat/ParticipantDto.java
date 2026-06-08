package com.trototvn.trototandroid.data.model.chat;

import com.google.gson.annotations.SerializedName;

public class ParticipantDto {

    @SerializedName("customerId")
    public long customerId;

    @SerializedName("firstName")
    public String firstName;

    @SerializedName("lastName")
    public String lastName;

    @SerializedName("avatarId")
    public Long avatarId;

    @SerializedName("userId")
    public Long userId;

    @SerializedName("accountId")
    public Long accountId;

    @SerializedName("participantId")
    public Long participantId;

    @SerializedName("id")
    public Long id;

    /**
     * Lấy signaling ID của participant (ID dùng để gọi qua WebRTC/Socket.IO)
     * Ưu tiên: userId -> accountId -> participantId -> id -> fallback customerId
     */
    public long getSignalingId() {
        if (userId != null) {
            return userId;
        }
        if (accountId != null) {
            return accountId;
        }
        if (participantId != null) {
            return participantId;
        }
        if (id != null) {
            return id;
        }
        return customerId;
    }
}
