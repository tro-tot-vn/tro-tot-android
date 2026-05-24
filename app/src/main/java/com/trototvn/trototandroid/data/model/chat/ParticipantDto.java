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
}
