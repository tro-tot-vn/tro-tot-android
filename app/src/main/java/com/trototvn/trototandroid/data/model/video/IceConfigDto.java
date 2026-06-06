package com.trototvn.trototandroid.data.model.video;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * DTO đại diện cho danh sách cấu hình ICE Servers từ Backend
 */
public class IceConfigDto {

    @SerializedName("iceServers")
    public List<IceServerDto> iceServers;
}
