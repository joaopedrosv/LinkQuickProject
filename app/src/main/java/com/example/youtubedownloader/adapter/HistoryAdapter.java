package com.example.youtubedownloader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.youtubedownloader.R;
import com.example.youtubedownloader.data.DownloadHistory;
import com.example.youtubedownloader.repository.DownloadRepository;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    List<DownloadHistory> list;
    DownloadRepository repo;

    public HistoryAdapter(List<DownloadHistory> list, DownloadRepository repo) {
        this.list = list;
        this.repo = repo;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        DownloadHistory item = list.get(position);

        holder.title.setText(item.title != null ? item.title : item.url);
        
        if (item.thumbnail != null) {
            Glide.with(holder.itemView.getContext())
                .load(item.thumbnail)
                .into(holder.thumbnail);
        }

        holder.itemView.setOnLongClickListener(v -> {
            repo.delete(item);
            list.remove(position);
            notifyDataSetChanged();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView thumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.historyTitle);
            thumbnail = itemView.findViewById(R.id.historyThumbnail);
        }
    }
}