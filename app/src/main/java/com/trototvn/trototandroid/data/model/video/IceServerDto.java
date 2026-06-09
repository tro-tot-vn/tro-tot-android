package com.trototvn.trototandroid.data.model.video;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

/**
 * DTO đại diện cho cấu hình một ICE Server (STUN/TURN) từ Backend
 */
public class IceServerDto {

    @SerializedName("urls")
    public String urls;

    @SerializedName("username")
    @Nullable
    public String username;

    @SerializedName("credential")
    @Nullable
    public String credential;
}
