package com.trototvn.trototandroid.data.model.video;

import com.google.gson.annotations.SerializedName;

/**
 * Lớp phong bì (Envelope) bọc payload của mọi sự kiện Socket.IO từ Backend
 * @param <T> Kiểu dữ liệu payload thực tế
 */
public class SocketEnvelope<T> {

    @SerializedName("status")
    public int status;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public T data;
}
