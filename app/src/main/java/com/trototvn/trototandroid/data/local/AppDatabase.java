package com.trototvn.trototandroid.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.trototvn.trototandroid.data.local.converter.DateConverter;
import com.trototvn.trototandroid.data.local.dao.ChatDao;
import com.trototvn.trototandroid.data.local.entity.ConversationEntity;
import com.trototvn.trototandroid.data.local.entity.ConversationParticipantEntity;
import com.trototvn.trototandroid.data.local.entity.MessageAttachmentEntity;
import com.trototvn.trototandroid.data.local.entity.MessageEntity;

@Database(entities = {
        MessageEntity.class,
        ConversationEntity.class,
        ConversationParticipantEntity.class,
        MessageAttachmentEntity.class
}, version = 1, exportSchema = false)
@TypeConverters({ DateConverter.class })
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "tro_tot_database";

    private static volatile AppDatabase INSTANCE;

    public abstract ChatDao chatDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
