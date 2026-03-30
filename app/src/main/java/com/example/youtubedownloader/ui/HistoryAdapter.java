package com.example.youtubedownloader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

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

        holder.url.setText(item.url);

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
        TextView url;

        public ViewHolder(View itemView) {
            super(itemView);
            url = itemView.findViewById(R.id.txtUrl);
        }
    }
}