package com.example.btl1.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.ListFragment;

import com.example.btl1.R;
import com.example.btl1.Adapter.SongAdapter;
import com.example.btl1.DB.FavoritesOperations;
import com.example.btl1.Model.SongsList;

import java.util.ArrayList;

public class FavSongFragment extends ListFragment {

    private static final String ARG_IMAGE = "album_image";

    private FavoritesOperations favoritesOperations;
    private ArrayList<SongsList> songsList;
    private ArrayList<SongsList> filteredList;

    private ListView listView;
    private CreateDataParsed callback;

    private int imageResId = R.drawable.favsongalbum;
    private String currentPlayingPath = "";

    public static FavSongFragment newInstance(int imageResId) {
        FavSongFragment fragment = new FavSongFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE, imageResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (CreateDataParsed) context;
        favoritesOperations = new FavoritesOperations(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_allsong, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            imageResId = getArguments().getInt(ARG_IMAGE, R.drawable.favsongalbum);
        }

        ImageView albumImage = view.findViewById(R.id.imageAlbum);
        TextView albumTitle = view.findViewById(R.id.textAlbumTitle);
        TextView songCount = view.findViewById(R.id.textSongCount);

        albumImage.setImageResource(imageResId);
        albumTitle.setText("DANH SÁCH YÊU THÍCH");

        listView = view.findViewById(R.id.ListViewSong);

        currentPlayingPath = callback.getCurrentPath();
        setContent();

        songCount.setText(songsList.size() + " bài hát");
    }

    private void setContent() {
        boolean isFiltered = false;
        songsList = favoritesOperations.getAllFavorites();
        filteredList = new ArrayList<>();

        SongAdapter adapter = new SongAdapter(getContext(), songsList, currentPlayingPath);
        if (!callback.queryText().isEmpty()) {
            adapter = onQueryTextChange();
            isFiltered = true;
        }

        listView.setAdapter(adapter);

        final boolean finalFiltered = isFiltered;
        listView.setOnItemClickListener((parent, view, position, id) -> {
            SongsList song = finalFiltered ? filteredList.get(position) : songsList.get(position);
            currentPlayingPath = song.getPath();
            callback.onDataPass(song.getSongsTitle(), song.getPath());
            callback.fullSongList(songsList, songsList.indexOf(song));

            SongAdapter newAdapter = new SongAdapter(getContext(), songsList, currentPlayingPath);
            listView.setAdapter(newAdapter);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            deleteOption(position);
            return true;
        });
    }

    private void deleteOption(int position) {
        SongsList selectedSong = songsList.get(position);
        if (!selectedSong.getPath().equals(currentPlayingPath)) {
            showDialog(selectedSong.getPath(), position);
        } else {
            Toast.makeText(getContext(), "Bạn không được xóa bài hát đang phát!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDialog(final String songPath, final int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.delete_text))
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    favoritesOperations.removeSong(songPath);
                    setContent(); // cập nhật lại
                })
                .show();
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

    public interface CreateDataParsed {
        void onDataPass(String name, String path);
        void fullSongList(ArrayList<SongsList> songList, int position);
        int getPosition();
        String queryText();
        String getCurrentPath(); // ✅ thêm hàm này để highlight
    }
}
