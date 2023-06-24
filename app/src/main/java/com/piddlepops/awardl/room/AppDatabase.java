package com.piddlepops.awardl.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {HistoryEntity.class}, version = 1)

public abstract class AppDatabase extends RoomDatabase {

    public abstract HistoryDao userDao();

    // Singleton instance
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "my-database").allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
