package com.polyap.music_player;


import static com.polyap.music_player.AlbumFragment.albums;
import static com.polyap.music_player.MainActivity.currentMusicPlaying;
import static com.polyap.music_player.MainActivity.musicFiles;
import static com.polyap.music_player.MainActivity.oldMusicPlayed;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.ViewHolder> {
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    private final Context context;
    static ArrayList<MusicFiles> albumFiles;
    private int lastPosition = -1;
    AlbumDetailsAdapter(Context context, ArrayList<MusicFiles> musicFilesList) {
        this.albumFiles = musicFilesList;
        this.context = context;
    }
    @Override
    public AlbumDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_items, parent, false);
        return new AlbumDetailsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumDetailsAdapter.ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.recycleview_animation_down
                        : R.anim.recycleview_animation_up);
        holder.itemView.startAnimation(animation);
        lastPosition = position;
        if(currentMusicPlaying != null && albumFiles.get(position).getId().equals(currentMusicPlaying.getId())) {
            holder.equalizer.setVisibility(View.VISIBLE);
            holder.equalizer.animateBars();
            // holder.fileName.setTextColor(view.getResources().getColor(R.color.purple_200));
            holder.fileName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            holder.fileName.setMarqueeRepeatLimit(-1);
            holder.fileName.setHorizontallyScrolling(true);
            holder.fileName.setSingleLine(true);
            holder.fileName.setSelected(true);
            holder.imageBackground.setBackgroundResource(R.color.purple_200);
        }
        else {
            holder.equalizer.stopBars();
            holder.fileName.setSelected(false);
            holder.equalizer.setVisibility(View.GONE);
            holder.imageBackground.setBackgroundResource(0);
        }
        holder.fileName.setText(albumFiles.get(position).getTitle());
        holder.artistName.setText(albumFiles.get(position).getArtist());
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(albumFiles.get(position).getAlbumId()));
        if(imageUri != null){
            Glide.with(context).load(imageUri).into(holder.albumArt);
        }
        if(holder.albumArt.getDrawable() == null) {
            holder.albumArt.setImageResource(R.drawable.msc_back1);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("sender", "AlbumDetails");
                intent.putExtra("position", position);
                Log.d("blyatpos", String.valueOf(position));
                /*oldMusicPlayed =  currentMusicPlaying;

                if(oldMusicPlayed!= null) {
                    RecyclerView.Adapter albumDetailsAdapter = AlbumDetails.recyclerView.getAdapter();
                    if(albumDetailsAdapter != null){
                        albumDetailsAdapter.notifyItemChanged(albumFiles.indexOf(oldMusicPlayed));
                    }
                }
                currentMusicPlaying = albumFiles.get(position);*/

                holder.equalizer.setVisibility(View.VISIBLE);
                holder.equalizer.animateBars();
                // holder.fileName.setTextColor(view.getResources().getColor(R.color.purple_200));
                holder.imageBackground.setBackgroundResource(R.color.purple_200);
                context.startActivity(intent);
            }
        });

        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
                    @Override
                    public void onItemClick(int itemPosition, PowerMenuItem item) {
                        if(itemPosition == 0){
                            deleteFile(position, view);
                        }
                    }
                };
                PowerMenu recycleMenu = new PowerMenu.Builder(context)
                        .addItem(new PowerMenuItem("Delete", true))
                        .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                        .setMenuRadius(30f)
                        .setMenuShadow(10f)
                        .setSelectedEffect(true)
                        .setOnMenuItemClickListener(onMenuItemClickListener)
                        .setAutoDismiss(true)
                        .build();

                recycleMenu.showAsAnchorLeftTop(view);
            }
        });

    }


    private void deleteFile(int position, View view){
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(albumFiles.get(position).getId()));
        File file = new File(albumFiles.get(position).getPath());
        boolean delete = file.delete();
        if(delete) {
            context.getContentResolver().delete(contentUri, null, null);
            if(musicFiles != null) {
                int indx = musicFiles.indexOf(albumFiles.get(position));
                if(indx != -1) {
                    musicFiles.remove(indx);
                    notifyItemRangeChanged(indx, musicFiles.size());
                }
            }
            if(albums != null){
                int indx = albums.indexOf(albumFiles.get(position));
                if(indx != -1) {
                    albums.remove(indx);
                    notifyItemRangeChanged(indx, albums.size());
                }
            }
            albumFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, albumFiles.size());

            if(SongFragment.recyclerViewSong != null) {
                RecyclerView.Adapter songAdapter = SongFragment.recyclerViewSong.getAdapter();
                if (songAdapter != null) {
                    songAdapter.notifyDataSetChanged();
                }
            }
            if(AlbumFragment.recyclerViewAlbum != null) {
                RecyclerView.Adapter albumAdapter = AlbumFragment.recyclerViewAlbum.getAdapter();
                if (albumAdapter != null) {
                    albumAdapter.notifyDataSetChanged();
                }
            }
            Snackbar.make(view, "Deleted : ", Snackbar.LENGTH_LONG).show();
        }
        else {
            Snackbar.make(view, "Can't be deleted : ", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView albumArt;//Shapeable
        final TextView fileName;
        final TextView artistName;
        final ImageView menuMore;
        ImageView imageBackground;
        EqualizerView equalizer;

        ViewHolder(View view){
            super(view);
            fileName = view.findViewById(R.id.music_file_name);
            albumArt = view.findViewById(R.id.music_img);
            artistName = view.findViewById(R.id.recycle_view_artist);
            menuMore = view.findViewById(R.id.menu_more);
            equalizer = (EqualizerView) view.findViewById(R.id.equalizer_view);
            imageBackground = view.findViewById(R.id.image_background);
        }
    }
}