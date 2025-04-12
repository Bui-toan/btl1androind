package com.example.btl1.Fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.btl1.Activity.MainActivity;
import com.example.btl1.DB.FavoritesOperations;
import com.example.btl1.Model.SongsList;
import com.example.btl1.R;
import com.example.btl1.Service.MusicService;

import java.util.ArrayList;

public class PlayerFragment extends Fragment {

    private static final String ARG_SONG_LIST = "song_list";
    private static final String ARG_POSITION = "position";

    private ArrayList<SongsList> songsList;
    private int currentPosition;

    private MusicService musicService;
    private Handler handler;
    private Runnable runnable;

    private TextView tvStartTime, tvFinalTime, tvSongTitle;
    private ImageButton btnPlayPause, btnNext, btnPrev, btnReplay, btnFav;
    private SeekBar seekBar;
    private boolean isLooping = false;

    private FavoritesOperations favoritesOperations;
    private GestureDetector gestureDetector;

    private ImageView imgAlbumArt;
    private ObjectAnimator discAnimator;

    public static PlayerFragment newInstance(ArrayList<SongsList> list, int position) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_SONG_LIST, list);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        if (getArguments() != null) {
            songsList = getArguments().getParcelableArrayList(ARG_SONG_LIST);
            currentPosition = getArguments().getInt(ARG_POSITION, 0);
        }

        musicService = ((MainActivity) requireActivity()).getMusicService();
        favoritesOperations = new FavoritesOperations(requireContext());

        if (songsList == null) songsList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnPlayPause = view.findViewById(R.id.ImageButtonPlay);
        btnNext = view.findViewById(R.id.ImageButtonNext);
        btnPrev = view.findViewById(R.id.ImageButtonPrevious);
        btnReplay = view.findViewById(R.id.ImageButtonReplay);
        btnFav = view.findViewById(R.id.ImageButtonFavorite);

        tvStartTime = view.findViewById(R.id.TextViewStartTime);
        tvFinalTime = view.findViewById(R.id.TextViewFinalTime);
        tvSongTitle = view.findViewById(R.id.TextViewSongTitle);
        seekBar = view.findViewById(R.id.SeekBar);
        imgAlbumArt = view.findViewById(R.id.imgAlbumArt);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        setupDiscAnimation();

        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null && (e2.getY() - e1.getY()) > 150 && Math.abs(velocityY) > 200) {
                    requireActivity().onBackPressed();
                    return true;
                }
                return false;
            }
        });

        view.setClickable(true);
        view.setFocusableInTouchMode(true);
        view.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        btnReplay.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white));

        if (!songsList.isEmpty()) {
            playSong(currentPosition);
        }

        btnPlayPause.setOnClickListener(v -> {
            if (musicService.isPlaying()) {
                musicService.pause();
                btnPlayPause.setImageResource(R.drawable.play_icon);
                pauseDiscRotation();
            } else {
                musicService.resume();
                btnPlayPause.setImageResource(R.drawable.pause_icon);
                updateSeekBar();
                resumeDiscRotation();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPosition + 1 < songsList.size()) {
                currentPosition++;
            } else {
                currentPosition = 0;
                Toast.makeText(getContext(), "Đang phát lại từ đầu 🎶", Toast.LENGTH_SHORT).show();
            }
            playSong(currentPosition);
        });

        btnPrev.setOnClickListener(v -> {
            if (currentPosition - 1 >= 0) {
                currentPosition--;
            } else {
                currentPosition = songsList.size() - 1;
                Toast.makeText(getContext(), "Chuyển đến bài cuối 🎵", Toast.LENGTH_SHORT).show();
            }
            playSong(currentPosition);
        });

        btnReplay.setOnClickListener(v -> {
            isLooping = !isLooping;
            musicService.setLooping(isLooping);
            btnReplay.setColorFilter(ContextCompat.getColor(requireContext(),
                    isLooping ? R.color.purple_700 : android.R.color.white));
            Toast.makeText(getContext(),
                    isLooping ? "Phát lại!" : "Dừng phát lại!", Toast.LENGTH_SHORT).show();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicService.seekTo(progress);
                    tvStartTime.setText(formatTime(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnFav.setOnClickListener(v -> {
            SongsList song = songsList.get(currentPosition);
            ArrayList<SongsList> favList = favoritesOperations.getAllFavorites();
            boolean isFavorite = favList.stream().anyMatch(s -> s.getPath().equals(song.getPath()));

            if (isFavorite) {
                favoritesOperations.removeSong(song.getPath());
                btnFav.setImageResource(R.drawable.favorite_icon);
                Toast.makeText(getContext(), "Đã xóa khỏi Yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                favoritesOperations.addSongFav(song);
                btnFav.setImageResource(R.drawable.ic_favorite_filled);
                Toast.makeText(getContext(), "Đã thêm vào Yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playSong(int pos) {
        SongsList song = songsList.get(pos);
        tvSongTitle.setText(song.getSongsTitle());

        if (!song.getPath().equals(musicService.getCurrentPath())) {
            musicService.playSong(song.getPath());
        }

        btnPlayPause.setImageResource(
                musicService.isPlaying() ? R.drawable.pause_icon : R.drawable.play_icon
        );

        updateFavoriteIcon(song);
        seekBar.setMax(musicService.getDuration());
        tvFinalTime.setText(formatTime(musicService.getDuration()));
        updateSeekBar();

        resetAndStartDiscRotation();

        musicService.setOnCompletionListener(mp -> {
            currentPosition = (currentPosition + 1) % songsList.size();
            playSong(currentPosition);
        });
    }

    private void setupDiscAnimation() {
        discAnimator = ObjectAnimator.ofFloat(imgAlbumArt, "rotation", 0f, 360f);
        discAnimator.setDuration(10000);
        discAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        discAnimator.setInterpolator(new LinearInterpolator());
    }

    private void resetAndStartDiscRotation() {
        if (discAnimator != null) {
            discAnimator.cancel();
        }

        imgAlbumArt.setRotation(0f);

        discAnimator = ObjectAnimator.ofFloat(imgAlbumArt, "rotation", 0f, 360f);
        discAnimator.setDuration(10000);
        discAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        discAnimator.setInterpolator(new LinearInterpolator());
        discAnimator.start();
    }

    private void resumeDiscRotation() {
        if (discAnimator != null) {
            if (discAnimator.isPaused()) {
                discAnimator.resume();
            } else if (!discAnimator.isRunning()) {
                discAnimator.start();
            }
        }
    }

    private void pauseDiscRotation() {
        if (discAnimator != null && discAnimator.isRunning()) {
            discAnimator.pause();
        }
    }

    private void updateFavoriteIcon(SongsList song) {
        ArrayList<SongsList> favList = favoritesOperations.getAllFavorites();
        boolean isFavorite = favList.stream().anyMatch(s -> s.getPath().equals(song.getPath()));
        btnFav.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.favorite_icon);
    }

    private void updateSeekBar() {
        if (musicService != null && musicService.isPlaying()) {
            int pos = musicService.getCurrentPosition();
            seekBar.setProgress(pos);
            tvStartTime.setText(formatTime(pos));
            runnable = this::updateSeekBar;
            handler.postDelayed(runnable, 500);
        }
    }

    private String formatTime(int ms) {
        int minutes = ms / 60000;
        int seconds = (ms / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
        if (discAnimator != null) discAnimator.cancel();
        musicService.setOnCompletionListener(null);
    }
}
