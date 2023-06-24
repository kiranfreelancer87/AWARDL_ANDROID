package com.piddlepops.awardl.room;

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

}
