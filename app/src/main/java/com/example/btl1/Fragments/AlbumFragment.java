package com.example.btl1.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.btl1.Activity.MainActivity;
import com.example.btl1.R;

public class AlbumFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_list, container, false);
        LinearLayout albumList = view.findViewById(R.id.albumList);

        View album1 = inflater.inflate(R.layout.album_item, albumList, false);
        ((TextView) album1.findViewById(R.id.textViewUpdate)).setText("MỚI CẬP NHẬT");
        ((TextView) album1.findViewById(R.id.textViewAlbumName)).setText("Tất cả bài hát");
        ((TextView) album1.findViewById(R.id.textViewFirstSong)).setText("Nghe ngay nào 🎶");
        ((ImageView) album1.findViewById(R.id.imageViewAlbumBackground)).setImageResource(R.drawable.allsongalbum);
        album1.setOnClickListener(v -> {
            AllSongFragment fragment = AllSongFragment.newInstance(R.drawable.allsongalbum);
            ((MainActivity) requireActivity()).loadAlbumFragment(fragment);
        });

        View album2 = inflater.inflate(R.layout.album_item, albumList, false);
        ((TextView) album2.findViewById(R.id.textViewUpdate)).setText("YÊU THÍCH");
        ((TextView) album2.findViewById(R.id.textViewAlbumName)).setText("Danh sách yêu thích");
        ((TextView) album2.findViewById(R.id.textViewFirstSong)).setText("Những bài hát bạn thích");
        ((ImageView) album2.findViewById(R.id.imageViewAlbumBackground)).setImageResource(R.drawable.favsongalbum);
        album2.setOnClickListener(v -> {
            FavSongFragment fragment = FavSongFragment.newInstance(R.drawable.favsongalbum);
            ((MainActivity) requireActivity()).loadAlbumFragment(fragment);
        });

        albumList.addView(album1);
        albumList.addView(album2);

        return view;
    }
}
