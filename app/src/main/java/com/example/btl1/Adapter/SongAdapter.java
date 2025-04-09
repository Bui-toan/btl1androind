package com.example.btl1.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.btl1.R;
import com.example.btl1.Model.SongsList;

import java.util.ArrayList;

public class SongAdapter extends ArrayAdapter<SongsList> implements Filterable {

    private final Context mContext;
    private final ArrayList<SongsList> songList;

    public SongAdapter(Context context, ArrayList<SongsList> songs) {
        super(context, 0, songs);
        this.mContext = context;
        this.songList = songs;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.playlist_items, parent, false);
        }

        SongsList currentSong = songList.get(position);

        TextView tvTitle = view.findViewById(R.id.TextViewSongsTitle);
        TextView tvSubtitle = view.findViewById(R.id.TextViewArtistTitle);

        tvTitle.setText(currentSong.getSongsTitle());
        tvSubtitle.setText(currentSong.getArtistTitle());

        return view;
    }
}
