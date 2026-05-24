package com.trototvn.trototandroid.data.local.converter;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Converter used for room entity and data from server
 */
public class DateConverter {

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
}
