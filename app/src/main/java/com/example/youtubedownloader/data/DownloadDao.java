package com.example.youtubedownloader.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DownloadDao {

    @Insert
    void insert(DownloadHistory history);

    @Query("SELECT * FROM DownloadHistory ORDER BY date DESC")
    List<DownloadHistory> getAll();

    @Delete
    void delete(DownloadHistory history);
}