package com.example.btl1.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class SongsList implements Parcelable {

    private String title;
    private String artistTitle;
    private String path;

    public SongsList(String title, String artistTitle, String path) {
        this.title = title;
        this.artistTitle = artistTitle;
        this.path = path;
    }

    protected SongsList(Parcel in) {
        title = in.readString();
        artistTitle = in.readString();
        path = in.readString();
    }

    public static final Creator<SongsList> CREATOR = new Creator<SongsList>() {
        @Override
        public SongsList createFromParcel(Parcel in) {
            return new SongsList(in);
        }

        @Override
        public SongsList[] newArray(int size) {
            return new SongsList[size];
        }
    };

    public String getSongsTitle() { return title; }
    public void setSongsTitle(String title) { this.title = title; }

    public String getArtistTitle() { return artistTitle; }
    public void setArtistTitle(String artistTitle) { this.artistTitle = artistTitle; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artistTitle);
        dest.writeString(path);
    }
}
