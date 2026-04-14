package com.trototvn.trototandroid.data.local.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trototvn.trototandroid.data.model.chat.AttachmentDto;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatTypeConverters {

    private static final Gson gson = new Gson();

    @TypeConverter
    public String fromAttachmentDtoList(List<AttachmentDto> attachments) {
        if (attachments == null) {
            return null;
        }
        if (attachments.isEmpty()) {
            return "[]";
        }
        return gson.toJson(attachments);
    }

    @TypeConverter
    public List<AttachmentDto> toAttachmentDtoList(String attachmentsString) {
        if (attachmentsString == null || attachmentsString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<AttachmentDto>>() {}.getType();
        return gson.fromJson(attachmentsString, listType);
    }
}
