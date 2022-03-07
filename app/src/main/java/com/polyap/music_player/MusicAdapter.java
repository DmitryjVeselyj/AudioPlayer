package com.polyap.music_player;


import static com.polyap.music_player.MainActivity.isVisualize;

import android.content.ContentUris;
import android.content.Context;

import android.content.Intent;
import android.net.Uri;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.File;
import java.util.List;

public class MusicAdapter  extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    private final Context context;
    private final List<MusicFiles> musicFilesList;
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

        holder.fileName.setText(musicFilesList.get(position).getTitle());
        holder.artistName.setText(musicFilesList.get(position).getArtist());
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(musicFilesList.get(position).getAlbumId()));
        if(imageUri != null){
            Glide.with(context).load(imageUri).into(holder.albumArt);
        }
        if(holder.albumArt.getDrawable() == null)
            holder.albumArt.setImageResource(R.drawable.msc_back);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("position", position);
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

                recycleMenu.showAsDropDown(view);
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
            musicFilesList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, musicFilesList.size());
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
        ViewHolder(View view){
            super(view);
            fileName = view.findViewById(R.id.music_file_name);
            albumArt = view.findViewById(R.id.music_img);
            artistName = view.findViewById(R.id.recycle_view_artist);
            menuMore = view.findViewById(R.id.menu_more);
        }
    }
}

