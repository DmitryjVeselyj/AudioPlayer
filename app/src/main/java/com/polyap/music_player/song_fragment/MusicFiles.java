package com.polyap.music_player.song_fragment;

import java.io.Serializable;

/**
 * Класс, который а ля "трек"
 */
public class MusicFiles implements Serializable {
    private String title;
    private String album;
    private String artist;
    private String duration;
    private String path;

    private String albumId;
    private String id;
    private String size;

    //Комментатор отказался давать комментарии
    public MusicFiles() {

    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    private String dateAdded;

    public MusicFiles(String title, String album, String artist, String duration, String path, String albumId, String id, String dateAdded, String size) {
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.path = path;
        this.albumId = albumId;
        this.id = id;
        this.dateAdded = dateAdded;
        this.size = size;
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

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
