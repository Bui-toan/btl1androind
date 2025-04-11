package com.example.btl1.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import com.example.btl1.Adapter.SongAdapter;
import com.example.btl1.Model.SongsList;
import com.example.btl1.R;

import java.util.ArrayList;

public class AllSongFragment extends ListFragment {

    private static final String ARG_IMAGE_RES = "album_image";
    private int albumImageResId = R.drawable.allsongalbum;

    private ArrayList<SongsList> songsList = new ArrayList<>();
    private ArrayList<SongsList> filteredList = new ArrayList<>();
    private ListView listView;
    private CreateDataParse callback;

    private String currentPlayingPath = "";

    public interface CreateDataParse {
        void onDataPass(String name, String path);
        void fullSongList(ArrayList<SongsList> songList, int position);
        String queryText();
        void currentSong(SongsList song);
        void getLength(int length);
        String getCurrentPath(); // ✅ lấy path bài đang phát
    }

    public static AllSongFragment newInstance(int imageResId) {
        AllSongFragment fragment = new AllSongFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RES, imageResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (CreateDataParse) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            albumImageResId = getArguments().getInt(ARG_IMAGE_RES, R.drawable.allsongalbum);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_allsong, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageView imageAlbum = view.findViewById(R.id.imageAlbum);
        imageAlbum.setImageResource(albumImageResId);

        listView = view.findViewById(R.id.ListViewSong);
        currentPlayingPath = callback.getCurrentPath(); // ✅ lấy path hiện tại
        setContent();
    }

    private void setContent() {
        boolean isFiltered = false;
        songsList.clear();
        filteredList.clear();

        getMusic();

        TextView textCount = getView().findViewById(R.id.textSongCount);
        textCount.setText(songsList.size() + " bài hát");

        SongAdapter adapter = new SongAdapter(getContext(), songsList, currentPlayingPath);

        if (!callback.queryText().isEmpty()) {
            adapter = onQueryTextChange();
            isFiltered = true;
        }

        callback.getLength(songsList.size());
        listView.setAdapter(adapter);

        final boolean finalIsFiltered = isFiltered;

        listView.setOnItemClickListener((parent, view, position, id) -> {
            SongsList song = finalIsFiltered ? filteredList.get(position) : songsList.get(position);
            currentPlayingPath = song.getPath(); // ✅ lưu lại path đang phát

            callback.onDataPass(song.getSongsTitle(), song.getPath());
            callback.fullSongList(songsList, songsList.indexOf(song));

            SongAdapter newAdapter = new SongAdapter(getContext(), songsList, currentPlayingPath);
            listView.setAdapter(newAdapter);
        });
    }

    private void getMusic() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int pathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                SongsList song = new SongsList(
                        cursor.getString(titleIndex),
                        cursor.getString(artistIndex),
                        cursor.getString(pathIndex)
                );
                songsList.add(song);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private SongAdapter onQueryTextChange() {
        String query = callback.queryText().toLowerCase();
        for (SongsList song : songsList) {
            if (song.getSongsTitle().toLowerCase().contains(query)) {
                filteredList.add(song);
            }
        }
        return new SongAdapter(getContext(), filteredList, currentPlayingPath);
    }
}
