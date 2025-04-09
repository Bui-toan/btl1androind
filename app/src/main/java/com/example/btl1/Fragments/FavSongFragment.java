package com.example.btl1.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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

    private FavoritesOperations favoritesOperations;
    private ArrayList<SongsList> songsList;
    private ArrayList<SongsList> filteredList;

    private ListView listView;
    private CreateDataParsed callback;

    public static FavSongFragment getInstance(int position) {
        FavSongFragment fragment = new FavSongFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (CreateDataParsed) context;
        favoritesOperations = new FavoritesOperations(context);
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

    private void setContent() {
        boolean isFiltered = false;
        songsList = favoritesOperations.getAllFavorites();
        filteredList = new ArrayList<>();

        SongAdapter adapter = new SongAdapter(getContext(), songsList);
        if (!callback.queryText().isEmpty()) {
            adapter = onQueryTextChange();
            isFiltered = true;
        }

        listView.setAdapter(adapter);

        final boolean finalFiltered = isFiltered;
        listView.setOnItemClickListener((parent, view, position, id) -> {
            SongsList song = finalFiltered ? filteredList.get(position) : songsList.get(position);
            callback.onDataPass(song.getSongsTitle(), song.getPath());
            callback.fullSongList(songsList, position);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            deleteOption(position);
            return true;
        });
    }

    private void deleteOption(int position) {
        if (position != callback.getPosition()) {
            showDialog(songsList.get(position).getPath(), position);
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
                    callback.fullSongList(songsList, position);
                    setContent();
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
        return new SongAdapter(getContext(), filteredList);
    }

    public interface CreateDataParsed {
        void onDataPass(String name, String path);
        void fullSongList(ArrayList<SongsList> songList, int position);
        int getPosition();
        String queryText();
    }
}
