package com.example.btl1.Model;


public class SongsList {

    private String title;
    private String artistTitle;
    private String path;

    public SongsList(String title, String artistTitle, String path) {
        this.title = title;
        this.artistTitle = artistTitle;
        this.path = path;
    }

    public String getSongsTitle() {
        return title;
    }

    public void setSongsTitle(String title) {
        this.title = title;
    }

    public String getArtistTitle() {
        return artistTitle;
    }

    public void setArtistTitle(String artistTitle) {
        this.artistTitle = artistTitle;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

