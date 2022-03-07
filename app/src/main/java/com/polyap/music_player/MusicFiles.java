package com.polyap.music_player;

import java.io.Serializable;

public class MusicFiles implements Serializable {
    private String title;
    private String album;
    private String artist;
    private String duration;
    private String path;

    private String albumId;
    private String id;



    public MusicFiles(String title, String album, String artist, String duration, String path, String albumId, String id) {
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.path = path;
        this.albumId= albumId;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumId() { return albumId; }

    public void setAlbumId(String albumId) { this.albumId = albumId; }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
