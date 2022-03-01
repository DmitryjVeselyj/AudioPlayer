package com.polyap.music_player;


import android.content.Context;

import android.net.Uri;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class MusicAdapter  extends RecyclerView.Adapter<MusicAdapter.ViewHolder>{
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    private final Context context;
    private final List<MusicFiles> musicFilesList;

    MusicAdapter(Context context, List<MusicFiles> musicFilesList) {
        this.musicFilesList = musicFilesList;
        this.context = context;
    }
    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.fileName.setText(musicFilesList.get(position).getTitle());
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(musicFilesList.get(position).getAlbumId()));
        if(imageUri != null){
            Glide.with(context).load(imageUri).into(holder.albumArt);
        }

    }

    @Override
    public int getItemCount() {
        return musicFilesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView albumArt;
        final TextView fileName;
        ViewHolder(View view){
            super(view);
            fileName = view.findViewById(R.id.music_file_name);
            albumArt = view.findViewById(R.id.music_img);
        }
    }
}

