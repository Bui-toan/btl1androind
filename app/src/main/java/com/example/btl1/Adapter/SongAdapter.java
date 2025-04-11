package com.example.btl1.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.btl1.Model.SongsList;
import com.example.btl1.R;

import java.util.ArrayList;

public class SongAdapter extends ArrayAdapter<SongsList> implements Filterable {

    private final Context mContext;
    private final ArrayList<SongsList> songList;
    private final String playingPath; // ✅ đường dẫn bài hát đang phát

    // ✅ Constructor cũ (không highlight)
    public SongAdapter(Context context, ArrayList<SongsList> songs) {
        super(context, 0, songs);
        this.mContext = context;
        this.songList = songs;
        this.playingPath = null; // không so sánh
    }

    // ✅ Constructor mới (highlight)
    public SongAdapter(Context context, ArrayList<SongsList> songs, String playingPath) {
        super(context, 0, songs);
        this.mContext = context;
        this.songList = songs;
        this.playingPath = playingPath;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.playlist_items, parent, false);
            holder = new ViewHolder();
            holder.tvTitle = convertView.findViewById(R.id.TextViewSongsTitle);
            holder.tvSubtitle = convertView.findViewById(R.id.TextViewArtistTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SongsList currentSong = songList.get(position);
        holder.tvTitle.setText(currentSong.getSongsTitle());
        holder.tvSubtitle.setText(currentSong.getArtistTitle());

        // ✅ Highlight bài đang phát
        if (playingPath != null && playingPath.equals(currentSong.getPath())) {
            convertView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            holder.tvTitle.setTextColor(Color.WHITE);
            holder.tvSubtitle.setTextColor(Color.LTGRAY);
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
            holder.tvTitle.setTextColor(Color.WHITE);
            holder.tvSubtitle.setTextColor(Color.GRAY);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvSubtitle;
    }
}
