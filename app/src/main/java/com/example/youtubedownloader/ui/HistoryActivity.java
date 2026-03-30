package com.example.youtubedownloader.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtubedownloader.R;
import com.example.youtubedownloader.adapter.HistoryAdapter;
import com.example.youtubedownloader.repository.DownloadRepository;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView recycler = findViewById(R.id.recyclerHistory);

        DownloadRepository repo = new DownloadRepository(this);

        HistoryAdapter adapter = new HistoryAdapter(repo.getAll(), repo);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
    }
}