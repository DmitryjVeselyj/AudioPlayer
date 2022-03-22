package com.polyap.music_player;

import static com.polyap.music_player.MainActivity.musicFiles;
import static com.polyap.music_player.MusicAdapter.musicFilesList;
import static com.polyap.music_player.PlayerActivity.getPosition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    private Context context;
    public ArrayList<MusicFiles> albumFilesFragment;
    private int lastPosition = -1;
    View view;
    public AlbumAdapter(Context context, ArrayList<MusicFiles> albumFiles){
        this.context = context;
        this.albumFilesFragment = albumFiles;
    }

    @NonNull
    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.recycleview_animation_down
                        : R.anim.recycleview_animation_up);
        holder.itemView.startAnimation(animation);
        lastPosition = position;

        holder.albumName.setText(albumFilesFragment.get(position).getAlbum());
        holder.albumArtist.setText(albumFilesFragment.get(position).getArtist());
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(albumFilesFragment.get(position).getAlbumId()));
        if(imageUri != null){
            Glide.with(context).load(imageUri).into(holder.albumImage);
        }
        if(holder.albumImage.getDrawable() == null)
            holder.albumImage.setImageResource(R.drawable.msc_back1);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AlbumDetails.class);
                intent.putExtra("albumName", albumFilesFragment.get(position).getAlbum())
                        .putExtra("artistName", albumFilesFragment.get(position).getArtist());
                intent.putExtra("position", getPosition((ArrayList<MusicFiles>) musicFiles, albumFilesFragment.get(position)));
                ActivityOptions options =
                        ActivityOptions.makeCustomAnimation(context, R.anim.zoom_in, R.anim.recycleview_animation_up );
               context.startActivity(intent);



            }
        });
    }


    @Override
    public int getItemCount() {
        return albumFilesFragment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView albumImage;
        TextView albumName;
        TextView albumArtist;
        public ViewHolder(View itemView){
            super(itemView);
            albumImage = itemView.findViewById(R.id.album_image);
            albumName = itemView.findViewById(R.id.album_name);
            albumArtist = itemView.findViewById(R.id.album_artist);
        }
    }

}
