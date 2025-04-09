package com.example.btl1.Fragments;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import com.example.btl1.R;
import com.example.btl1.Adapter.SongAdapter;
import com.example.btl1.Model.SongsList;

import java.util.ArrayList;

public class AllSongFragment extends ListFragment {

    private static ContentResolver staticResolver;

    private ArrayList<SongsList> songsList;
    private ArrayList<SongsList> filteredList;

    private ListView listView;
    private CreateDataParse callback;

    public static AllSongFragment getInstance(int position, ContentResolver resolver) {
        AllSongFragment fragment = new AllSongFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        fragment.setArguments(bundle);
        staticResolver = resolver;
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (CreateDataParse) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.ListViewSong);
        setContent();
    }

    public void setContent() {
        boolean isFiltered = false;
        songsList = new ArrayList<>();
        filteredList = new ArrayList<>();

        getMusic();

        SongAdapter adapter = new SongAdapter(getContext(), songsList);
        if (!callback.queryText().isEmpty()) {
            adapter = onQueryTextChange();
            isFiltered = true;
        }

        callback.getLength(songsList.size());
        listView.setAdapter(adapter);

        final boolean finalIsFiltered = isFiltered;
        listView.setOnItemClickListener((parent, view, position, id) -> {
            SongsList song = finalIsFiltered ? filteredList.get(position) : songsList.get(position);
            Log.d("CLICKED_SONG", "Đã click: " + song.getSongsTitle() + " - path: " + song.getPath());

            callback.onDataPass(song.getSongsTitle(), song.getPath());
            callback.fullSongList(songsList, position);
        });

    }

    private void getMusic() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = staticResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int pathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                songsList.add(new SongsList(
                        cursor.getString(titleIndex),
                        cursor.getString(artistIndex),
                        cursor.getString(pathIndex)
                ));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private SongAdapter onQueryTextChange() {
        String query = callback.queryText();
        for (SongsList song : songsList) {
            if (song.getSongsTitle().toLowerCase().contains(query)) {
                filteredList.add(song);
            }
        }
        return new SongAdapter(getContext(), filteredList);
    }

    public interface CreateDataParse {
        void onDataPass(String name, String path);
        void fullSongList(ArrayList<SongsList> songList, int position);
        String queryText();
        void currentSong(SongsList song);
        void getLength(int length);
    }
}
