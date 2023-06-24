package com.piddlepops.awardl.room;

import androidx.annotation.NonNull;
import androidx.annotation.UiContext;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "history")

public class HistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "game_time")
    public long game_time;

    @ColumnInfo(name = "word_len")
    public int word_len;

    @ColumnInfo(name = "time_consumed")
    public long time_consumed;

    @ColumnInfo(name = "attempts")
    public int attempts;

    @ColumnInfo(name = "word")
    public String word;

    @ColumnInfo(name = "hint_count")
    public int hint_count;

    @Override
    public String toString() {
        return "HistoryEntity{" +
                "id=" + id +
                ", game_time=" + game_time +
                ", time_consumed=" + time_consumed +
                ", attempts=" + attempts +
                ", word='" + word + '\'' +
                ", hint_count=" + hint_count +
                '}';
    }
}
