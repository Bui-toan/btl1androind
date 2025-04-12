package com.example.btl1.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.btl1.Activity.MainActivity;
import com.example.btl1.Adapter.SongAdapter;
import com.example.btl1.Model.SongsList;
import com.example.btl1.R;

import java.text.Normalizer;
import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private ArrayList<SongsList> filteredList = new ArrayList<>();
    private ListView listView;
    private SearchView searchView;
    private CreateDataParse callback;

    public interface CreateDataParse {
        void onDataPass(String name, String path);
        void fullSongList(ArrayList<SongsList> songList, int position);
        void currentSong(SongsList song);
        void getLength(int length);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CreateDataParse) {
            callback = (CreateDataParse) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement CreateDataParse");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.ListViewSong);
        searchView = view.findViewById(R.id.searchView);

        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadFilteredSongs(newText.trim().toLowerCase());
                return true;
            }
        });
    }

    private void loadFilteredSongs(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            listView.setAdapter(null);
            return;
        }

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int pathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            String queryNormalized = removeAccents(query.toLowerCase());

            do {
                String title = cursor.getString(titleIndex);
                String artist = cursor.getString(artistIndex);
                String path = cursor.getString(pathIndex);

                String titleNormalized = removeAccents(title.toLowerCase());

                if (titleNormalized.contains(queryNormalized)) {
                    SongsList song = new SongsList(title, artist, path);
                    filteredList.add(song);
                }

            } while (cursor.moveToNext());

            cursor.close();
        }

        SongAdapter adapter = new SongAdapter(getContext(), filteredList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            SongsList song = filteredList.get(position);
            callback.onDataPass(song.getSongsTitle(), song.getPath());
            callback.fullSongList(filteredList, position);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openPlayerFragment(filteredList, position);
            }
        });
    }

    private String removeAccents(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }
}
