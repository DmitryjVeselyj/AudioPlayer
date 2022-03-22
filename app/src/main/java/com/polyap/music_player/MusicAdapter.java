package com.polyap.music_player;


import static com.polyap.music_player.AlbumFragment.albums;
import static com.polyap.music_player.MainActivity.currentMusicPlaying;
import static com.polyap.music_player.MainActivity.isVisualize;
import static com.polyap.music_player.MainActivity.musicFiles;
import static com.polyap.music_player.MainActivity.oldMusicPlayed;

import android.content.ContentUris;
import android.content.Context;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class MusicAdapter  extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    private final Context context;
    static   List<MusicFiles> musicFilesList;
    private int lastPosition = -1;

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
        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.recycleview_animation_down
                        : R.anim.recycleview_animation_up);
        holder.itemView.startAnimation(animation);
        lastPosition = position;
        if( currentMusicPlaying != null && musicFilesList.get(position).getId().equals(currentMusicPlaying.getId())) {
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
            holder.equalizer.setVisibility(View.GONE);
            holder.fileName.setSelected(false);
            holder.imageBackground.setBackgroundResource(0);
        }
        holder.fileName.setText(musicFilesList.get(position).getTitle());
        holder.artistName.setText(musicFilesList.get(position).getArtist());
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(musicFilesList.get(position).getAlbumId()));
        if(imageUri != null){
            Glide.with(context).load(imageUri).into(holder.albumArt);
        }
        if(holder.albumArt.getDrawable() == null)
            holder.albumArt.setImageResource(R.drawable.msc_back1);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("sender", "MainActivity");
                intent.putExtra("position", position);



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
                Long.parseLong(musicFilesList.get(position).getId()));
        File file = new File(musicFilesList.get(position).getPath());
        boolean delete = file.delete();
        if(delete) {
            context.getContentResolver().delete(contentUri, null, null);
            if(albums!= null) {
                int indx = albums.indexOf(musicFiles.get(position));
                if(indx != -1) {
                    albums.remove(indx);
                    notifyItemRangeChanged(indx, albums.size());
                }
            }
            musicFilesList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, musicFilesList.size());

            if(AlbumFragment.recyclerViewAlbum != null) {
                RecyclerView.Adapter albumAdapter = AlbumFragment.recyclerViewAlbum.getAdapter();
                if (albumAdapter != null) {
                    albumAdapter.notifyDataSetChanged();//переписать для одного элемента, дабы все не обновлять
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
        return musicFilesList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView albumArt;//Shapeable
        final TextView fileName;
        final TextView artistName;
        final ImageView menuMore;
        ImageView imageBackground;
        EqualizerView equalizer;
        ConstraintLayout musicLayout;
        ViewHolder(View view){
            super(view);
            fileName = view.findViewById(R.id.music_file_name);
            albumArt = view.findViewById(R.id.music_img);
            artistName = view.findViewById(R.id.recycle_view_artist);
            menuMore = view.findViewById(R.id.menu_more);
            equalizer = (EqualizerView) view.findViewById(R.id.equalizer_view);
            musicLayout = view.findViewById(R.id.music_item_layout);
            imageBackground = view.findViewById(R.id.image_background);

        }
    }
    void updateList(ArrayList<MusicFiles> musicFilesArrayList){
        musicFilesList = new ArrayList<>();
        musicFilesList.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}

