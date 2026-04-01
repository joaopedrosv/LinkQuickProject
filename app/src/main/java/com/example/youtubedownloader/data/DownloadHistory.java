package com.example.youtubedownloader.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DownloadHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String url;
    public String title;
    public String thumbnail;
    public String filePath;
    public long date;
}