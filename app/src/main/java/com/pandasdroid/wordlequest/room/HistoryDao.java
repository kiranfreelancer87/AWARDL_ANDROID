package com.pandasdroid.wordlequest.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(HistoryEntity user);

    @Update
    void update(HistoryEntity user);

    @Delete
    void delete(HistoryEntity user);

    @Query("SELECT * FROM history")
    List<HistoryEntity> getHistory();

    @Query("SELECT * FROM history WHERE game_type = 1 AND word = :correctWord")
    List<HistoryEntity> getHistoryByGameTypeAndCorrectWord(String correctWord);

}
