package com.example.btl1.DB;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.btl1.Model.SongsList;

import java.util.ArrayList;

public class FavoritesOperations {

    private final SQLiteOpenHelper dbHandler;
    private SQLiteDatabase database;

    private static final String[] allColumns = {
            FavoritesDadaBaseHandler.COLUMN_ID,
            FavoritesDadaBaseHandler.COLUMN_TITLE,
            FavoritesDadaBaseHandler.COLUMN_SUBTITLE,
            FavoritesDadaBaseHandler.COLUMN_PATH
    };

    public FavoritesOperations(Context context) {
        dbHandler = new FavoritesDadaBaseHandler(context);
    }

    public void open() {
        database = dbHandler.getWritableDatabase();
    }

    public void close() {
        dbHandler.close();
    }

    public void addSongFav(SongsList songsList) {
        open();
        ContentValues values = new ContentValues();
        values.put(FavoritesDadaBaseHandler.COLUMN_TITLE, songsList.getSongsTitle());
        values.put(FavoritesDadaBaseHandler.COLUMN_SUBTITLE, songsList.getArtistTitle());
        values.put(FavoritesDadaBaseHandler.COLUMN_PATH, songsList.getPath());

        database.insertWithOnConflict(FavoritesDadaBaseHandler.TABLE_SONGS,
                null, values, SQLiteDatabase.CONFLICT_REPLACE);

        close();
    }

    public void removeSong(String songPath) {
        open();
        String whereClause = FavoritesDadaBaseHandler.COLUMN_PATH + "=?";
        String[] whereArgs = new String[]{songPath};

        database.delete(FavoritesDadaBaseHandler.TABLE_SONGS, whereClause, whereArgs);
        close();
    }

    public ArrayList<SongsList> getAllFavorites() {
        open();
        ArrayList<SongsList> favSongs = new ArrayList<>();
        Cursor cursor = database.query(FavoritesDadaBaseHandler.TABLE_SONGS,
                allColumns, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                SongsList song = new SongsList(
                        cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDadaBaseHandler.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDadaBaseHandler.COLUMN_SUBTITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDadaBaseHandler.COLUMN_PATH))
                );
                favSongs.add(song);
            } while (cursor.moveToNext());
            cursor.close();
        }
        close();
        return favSongs;
    }
}
