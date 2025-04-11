package com.example.btl1.Activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.btl1.Fragments.AlbumFragment;
import com.example.btl1.Fragments.AllSongFragment;
import com.example.btl1.Fragments.FavSongFragment;
import com.example.btl1.Fragments.PlayerFragment;
import com.example.btl1.Fragments.SearchFragment;
import com.example.btl1.Model.SongsList;
import com.example.btl1.R;
import com.example.btl1.Service.MusicService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        AllSongFragment.CreateDataParse,
        FavSongFragment.CreateDataParsed,
        SearchFragment.CreateDataParse {

    private final int MY_PERMISSION_REQUEST = 100;
    private MusicService musicService;
    private boolean isBound = false;

    private LinearLayout tabHome, tabSearch;
    private ImageView iconHome, iconSearch;
    private TextView textHome, textSearch;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        grantedPermission();
        bindService(new Intent(this, MusicService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        setupBottomNav();
    }

    private void initViews() {
        tabHome = findViewById(R.id.tabHome);
        tabSearch = findViewById(R.id.tabSearch);
        iconHome = findViewById(R.id.iconHome);
        iconSearch = findViewById(R.id.iconSearch);
        textHome = findViewById(R.id.textHome);
        textSearch = findViewById(R.id.textSearch);

        Toolbar toolbar = findViewById(R.id.Toolbar);
        toolbar.setTitle("Play Music");
        toolbar.setTitleTextColor(getResources().getColor(R.color.TextColor));
        setSupportActionBar(toolbar);
    }

    private void setupBottomNav() {
        setSelectedTab(R.id.tabHome); // Default tab

        tabHome.setOnClickListener(v -> {
            setSelectedTab(R.id.tabHome);
        });

        tabSearch.setOnClickListener(v -> {
            setSelectedTab(R.id.tabSearch);
        });
    }

    private void setSelectedTab(int tabId) {
        if (tabId == R.id.tabHome) {
            iconHome.setColorFilter(getColor(R.color.purple_700));
            textHome.setTextColor(getColor(R.color.purple_700));
            iconSearch.setColorFilter(getColor(android.R.color.darker_gray));
            textSearch.setTextColor(getColor(android.R.color.darker_gray));
            loadTabFragment(new AlbumFragment(), false); // <--- hiệu ứng lùi về
        } else if (tabId == R.id.tabSearch) {
            iconSearch.setColorFilter(getColor(R.color.purple_700));
            textSearch.setTextColor(getColor(R.color.purple_700));
            iconHome.setColorFilter(getColor(android.R.color.darker_gray));
            textHome.setTextColor(getColor(android.R.color.darker_gray));
            loadTabFragment(new SearchFragment(), true); // <--- hiệu ứng đi tới
        }
    }

    private void loadTabFragment(Fragment fragment, boolean forward) {
        int enter = forward ? R.anim.slide_in_right : R.anim.slide_in_left;
        int exit = forward ? R.anim.slide_out_left : R.anim.slide_out_right;

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(enter, exit, enter, exit)
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void loadAlbumFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.scale_in_center,   // fragment album mới mở vào
                        R.anim.scale_out_center,  // fragment hiện tại thoát ra
                        R.anim.scale_in_center,   // khi back: album hiện lại
                        R.anim.scale_out_center   // khi back: album thoát
                )
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public MusicService getMusicService() {
        return musicService;
    }

    public void openPlayerFragment(ArrayList<SongsList> list, int position) {
        PlayerFragment fragment = PlayerFragment.newInstance(list, position);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_up, R.anim.fade_out,
                        R.anim.fade_in, R.anim.slide_out_down
                )
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void grantedPermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_AUDIO};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSION_REQUEST);
        }
    }

    @Override public void onDataPass(String name, String path) {}
    @Override public void getLength(int length) {}

    @Override
    public String getCurrentPath() {
        return musicService != null ? musicService.getCurrentPath() : "";
    }

    @Override public void currentSong(SongsList song) {}
    @Override public int getPosition() { return 0; }

    @Override
    public void fullSongList(ArrayList<SongsList> songList, int position) {
        openPlayerFragment(songList, position);
    }

    @Override
    public String queryText() {
        return "";
    }

    @Override
    protected void onDestroy() {
        if (isBound) unbindService(serviceConnection);
        super.onDestroy();
    }
}
