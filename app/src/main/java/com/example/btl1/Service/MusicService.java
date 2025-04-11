package com.example.btl1.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;

import com.example.btl1.R;

public class MusicService extends Service {

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREV = "ACTION_PREV";

    private static final String CHANNEL_ID = "music_playback_channel";
    private static final int NOTIFICATION_ID = 1;

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;

    private String currentPath = "";
    private String currentTitle = "Đang phát";
    private String currentArtist = "Không rõ";

    private MediaPlayer.OnCompletionListener onCompletionListener;

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mediaSession = new MediaSessionCompat(this, "MusicService");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PREV);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(notificationReceiver, filter);
        }
    }

    public void playSong(String path) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            } else {
                mediaPlayer = new MediaPlayer();
            }

            currentPath = path;

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            currentTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            currentArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Gán listener hoàn tất nếu có
            if (onCompletionListener != null) {
                mediaPlayer.setOnCompletionListener(onCompletionListener);
            }

            startForeground(NOTIFICATION_ID, createNotification());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification();
        }
    }

    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateNotification();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        stopForeground(true);
        stopSelf();
    }

    public void seekTo(int pos) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(pos);
        }
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public void setLooping(boolean loop) {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(loop);
        }
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        this.onCompletionListener = listener;
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(listener);
        }
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case ACTION_PLAY:
                    resume();
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_NEXT:
                    if (playbackListener != null) playbackListener.onNext();
                    break;
                case ACTION_PREV:
                    if (playbackListener != null) playbackListener.onPrev();
                    break;
            }
        }
    };

    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    private Notification createNotification() {
        boolean isPlaying = isPlaying();
        Bitmap albumArt = getAlbumArt(currentPath);

        PendingIntent prevIntent = getPendingIntent(ACTION_PREV);
        PendingIntent playPauseIntent = getPendingIntent(isPlaying ? ACTION_PAUSE : ACTION_PLAY);
        PendingIntent nextIntent = getPendingIntent(ACTION_NEXT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_player)
                .setContentTitle(currentTitle != null ? currentTitle : "Không rõ tên")
                .setContentText(currentArtist != null ? currentArtist : "Không rõ ca sĩ")
                .setLargeIcon(albumArt)
                .addAction(R.drawable.previous_icon, "Trước", prevIntent)
                .addAction(isPlaying ? R.drawable.pause_icon : R.drawable.play_icon,
                        isPlaying ? "Dừng" : "Phát", playPauseIntent)
                .addAction(R.drawable.next_icon, "Tiếp", nextIntent)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .build();
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(action);
        return PendingIntent.getBroadcast(this, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private Bitmap getAlbumArt(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_player);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Phát nhạc",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        stopMusic();
        unregisterReceiver(notificationReceiver);
        super.onDestroy();
    }

    // Giao diện callback cho nút "Next/Prev" trong notification
    public interface PlaybackListener {
        void onNext();
        void onPrev();
    }

    private static PlaybackListener playbackListener;

    public static void setPlaybackListener(PlaybackListener listener) {
        playbackListener = listener;
    }
}
