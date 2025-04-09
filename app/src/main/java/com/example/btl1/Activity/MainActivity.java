package com.example.btl1.Activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.btl1.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.btl1.Adapter.ViewPagerAdapter;
import com.example.btl1.DB.FavoritesOperations;
import com.example.btl1.Fragments.AllSongFragment;
import com.example.btl1.Fragments.FavSongFragment;
import com.example.btl1.Model.SongsList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AllSongFragment.CreateDataParse, FavSongFragment.CreateDataParsed {

    private Menu menu;
    private ImageButton imgBtnPlayPause, imgbtnReplay, imgBtnPrev, imgBtnNext, imgBtnSetting;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SeekBar seekBar;
    private TextView tvStartTime, tvFinalTime;

    private ArrayList<SongsList> songList;
    private int Temp;
    private String searchText = "";
    private SongsList currSong;

    private boolean checkFlag = false, repeatFlag = false, playContinueFlag = false, favFlag = true, playlistFlag = false;
    private final int MY_PERMISSION_REQUEST = 100;
    private int allSongLength;

    MediaPlayer mediaPlayer;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        grantedPermission();
    }

    private void init() {
        imgBtnPrev = findViewById(R.id.ImageButtonPrevious);
        imgBtnNext = findViewById(R.id.ImageButtonNext);
        imgbtnReplay = findViewById(R.id.ImageButtonReplay);
        imgBtnSetting = findViewById(R.id.ImageButtonSetting);
        imgBtnPlayPause = findViewById(R.id.ImageButtonPlay);

        tvStartTime = findViewById(R.id.TextViewStartTime);
        tvFinalTime = findViewById(R.id.TextViewFinalTime);
        seekBar = findViewById(R.id.SeekBar);
        viewPager = findViewById(R.id.ViewPager);
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
        Toolbar toolbar = findViewById(R.id.Toolbar);
        FloatingActionButton refreshSongs = findViewById(R.id.FloatingActionButton);

        toolbar.setTitleTextColor(getResources().getColor(R.color.TextColor));
        setSupportActionBar(toolbar);

        imgBtnNext.setOnClickListener(this);
        imgBtnPrev.setOnClickListener(this);
        imgbtnReplay.setOnClickListener(this);
        refreshSongs.setOnClickListener(this);
        imgBtnPlayPause.setOnClickListener(this);
        imgBtnSetting.setOnClickListener(this);
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
        } else {
            setPagerLayout();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setPagerLayout();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Cần quyền truy cập bộ nhớ để hoạt động", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void setPagerLayout() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getContentResolver());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = findViewById(R.id.TabLayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                queryText();
                setPagerLayout();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_search) {
            Toast.makeText(this, "Tìm Kiếm", Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.menu_favorites) {
            if (checkFlag && mediaPlayer != null) {
                FavoritesOperations favoritesOperations = new FavoritesOperations(this);

                if (favFlag) {
                    Toast.makeText(this, "Thêm vào ưa thích !", Toast.LENGTH_SHORT).show();
                    item.setIcon(R.drawable.ic_favorite_filled);

                    SongsList favList = new SongsList(
                            songList.get(Temp).getSongsTitle(),
                            songList.get(Temp).getArtistTitle(),
                            songList.get(Temp).getPath()
                    );

                    favoritesOperations.addSongFav(favList);
                    favFlag = false;

                } else {
                    Toast.makeText(this, "Xóa khỏi yêu thích !", Toast.LENGTH_SHORT).show();
                    item.setIcon(R.drawable.favorite_icon);
                    favoritesOperations.removeSong(songList.get(Temp).getPath());
                    favFlag = true;
                }

                setPagerLayout(); // Cập nhật tab
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.ImageButtonPlay) {
            if (checkFlag) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    imgBtnPlayPause.setImageResource(R.drawable.play_icon);
                } else {
                    mediaPlayer.start();
                    imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
                    playCycle();
                }
            } else {
                Toast.makeText(this, "Vui lòng chọn bài hát !", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.FloatingActionButton) {
            Toast.makeText(this, "Làm mới", Toast.LENGTH_SHORT).show();
            setPagerLayout();

        } else if (id == R.id.ImageButtonReplay) {
            if (repeatFlag) {
                Toast.makeText(this, "Ngưng phát lại !", Toast.LENGTH_SHORT).show();
                mediaPlayer.setLooping(false);
                repeatFlag = false;
            } else {
                Toast.makeText(this, "Phát lại !", Toast.LENGTH_SHORT).show();
                mediaPlayer.setLooping(true);
                repeatFlag = true;
            }

        } else if (id == R.id.ImageButtonPrevious) {
            if (checkFlag) {
                if (mediaPlayer.getCurrentPosition() > 1) {
                    if (Temp - 1 > -1) {
                        attachMusic(songList.get(Temp - 1).getSongsTitle(), songList.get(Temp - 1).getPath());
                        Temp -= 1;
                    } else {
                        attachMusic(songList.get(Temp).getSongsTitle(), songList.get(Temp).getPath());
                    }
                } else {
                    attachMusic(songList.get(Temp).getSongsTitle(), songList.get(Temp).getPath());
                }
            } else {
                Toast.makeText(this, "Vui lòng chọn bài hát !", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.ImageButtonNext) {
            if (checkFlag) {
                if (Temp + 1 < songList.size()) {
                    attachMusic(songList.get(Temp + 1).getSongsTitle(), songList.get(Temp + 1).getPath());
                    Temp += 1;
                } else {
                    Toast.makeText(this, "Hết !", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Vui lòng chọn bài hát !", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.ImageButtonSetting) {
            playContinueFlag = !playContinueFlag;
            Toast.makeText(this, "Chức năng đang cập nhật ^ ^", Toast.LENGTH_SHORT).show();
        }
    }

    private void attachMusic(String name, String path) {
        imgBtnPlayPause.setImageResource(R.drawable.play_icon);
        setTitle(name);
        menu.getItem(1).setIcon(R.drawable.favorite_icon);
        favFlag = true;

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            setControls();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgBtnPlayPause.setImageResource(R.drawable.play_icon);
                if (playContinueFlag) {
                    if (Temp + 1 < songList.size()) {
                        attachMusic(songList.get(Temp + 1).getSongsTitle(), songList.get(Temp + 1).getPath());
                        Temp += 1;
                    } else {
                        Toast.makeText(MainActivity.this, "Hết !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void setControls() {
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        playCycle();
        checkFlag = true;
        if (mediaPlayer.isPlaying()) {
            imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
            tvFinalTime.setText(getTimeFormatted(mediaPlayer.getDuration()));
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    tvStartTime.setText(getTimeFormatted(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void playCycle() {
        try {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            tvStartTime.setText(getTimeFormatted(mediaPlayer.getCurrentPosition()));
            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        playCycle();

                    }
                };
                handler.postDelayed(runnable, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getTimeFormatted(long milliSeconds) {
        String finalTimerString = "";
        String secondsString;

        //Converting total duration into time
        int hours = (int) (milliSeconds / 3600000);
        int minutes = (int) (milliSeconds % 3600000) / 60000;
        int seconds = (int) ((milliSeconds % 3600000) % 60000 / 1000);

        // Adding hours if any
        if (hours > 0)
            finalTimerString = hours + ":";

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // Return timer String;
        return finalTimerString;
    }

    @Override
    public void onDataPass(String name, String path) {
        attachMusic(name, path);
    }

    @Override
    public void getLength(int length) {
        this.allSongLength = length;
    }

    @Override
    public void fullSongList(ArrayList<SongsList> songList, int position) {
        this.songList = songList;
        this.Temp = position;
        this.playlistFlag = songList.size() == allSongLength;
        this.playContinueFlag = !playlistFlag;
    }

    @Override
    public String queryText() {
        return searchText.toLowerCase();
    }

    @Override
    public void currentSong(SongsList songsList) {
        this.currSong = songsList;
    }

    @Override
    public int getPosition() {
        return Temp;
    }
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        handler.removeCallbacks(runnable);
    }
}
