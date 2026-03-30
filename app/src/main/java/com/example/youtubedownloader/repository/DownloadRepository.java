package com.example.youtubedownloader.repository;

import android.content.Context;

import com.example.youtubedownloader.data.AppDatabase;
import com.example.youtubedownloader.data.DownloadHistory;

import java.util.List;

public class DownloadRepository {

    private final AppDatabase db;

    public DownloadRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    public void insert(DownloadHistory history) {
        db.downloadDao().insert(history);
    }

    public List<DownloadHistory> getAll() {
        return db.downloadDao().getAll();
    }

    public void delete(DownloadHistory history) {
        db.downloadDao().delete(history);
    }
}