package com.polyap.music_player.album_details;


import static com.polyap.music_player.main_activity.MainActivity.SHOW_MINI_PLAYER;
import static com.polyap.music_player.main_activity.MainActivity.currentMusicPlaying;
import static com.polyap.music_player.main_activity.MainActivity.musicFiles;
import static com.polyap.music_player.main_activity.MainActivity.musicServiceMain;
import static com.polyap.music_player.player_activity.PlayerActivity.isPlaying;
import static com.polyap.music_player.album_fragment.AlbumFragment.albums;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.polyap.music_player.player_activity.PlayerActivity;
import com.polyap.music_player.R;
import com.polyap.music_player.album_fragment.AlbumFragment;
import com.polyap.music_player.song_fragment.MusicFiles;
import com.polyap.music_player.song_fragment.SongFragment;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.File;
import java.util.ArrayList;

import es.claucookie.miniequalizerlibrary.EqualizerView;

/**
 * Адаптер для активити AlbumDetails
 */
public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.ViewHolder> {
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    private final Context context;
    public static ArrayList<MusicFiles> albumFiles;
    public static ViewHolder lastAlbumHolder;
    private int lastPosition = -1;

    /**
     * Констуктор
     *
     * @param context        Контекст
     * @param musicFilesList список треков, которые будут отображаться
     */
    AlbumDetailsAdapter(Context context, ArrayList<MusicFiles> musicFilesList) {
        this.albumFiles = musicFilesList;
        this.context = context;
    }

    @Override
    /**
     * Создание ViewHolder-а
     * @param parent ViewGroup parent
     * @param viewType тип View
     * @return ViewHolder
     */
    public AlbumDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_items, parent, false);
        return new AlbumDetailsAdapter.ViewHolder(view);
    }

    @Override
    /**
     * В этом методе происходит "заполнение данными" каждого трека
     * @param holder ViewHolder
     * @param position позиция
     */
    public void onBindViewHolder(AlbumDetailsAdapter.ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.recycleview_animation_down
                        : R.anim.recycleview_animation_up);
        holder.itemView.startAnimation(animation);
        lastPosition = position;
        if (currentMusicPlaying != null && albumFiles.get(position).getId().equals(currentMusicPlaying.getId())) {
            lastAlbumHolder = holder;
            holder.equalizer.setVisibility(View.VISIBLE);
            //holder.equalizer.stopBars();
            if (isPlaying)
                holder.equalizer.animateBars();
            else
                holder.equalizer.stopBars();
            // holder.fileName.setTextColor(view.getResources().getColor(R.color.purple_200));

            holder.fileName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            holder.fileName.setMarqueeRepeatLimit(-1);
            holder.fileName.setHorizontallyScrolling(true);
            holder.fileName.setSingleLine(true);
            holder.fileName.setSelected(true);
            holder.imageBackground.setBackgroundResource(R.color.purple_200);
        } else {
            if (!holder.equalizer.isAnimating())
                holder.equalizer.stopBars();
            holder.equalizer.setVisibility(View.INVISIBLE);
            holder.fileName.setEllipsize(TextUtils.TruncateAt.END);
            holder.fileName.setSingleLine(true);
            holder.fileName.setSelected(false);
            holder.imageBackground.setBackgroundColor(Color.BLACK);
        }
        holder.fileName.setText(albumFiles.get(position).getTitle());
        holder.artistName.setText(albumFiles.get(position).getArtist());
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(albumFiles.get(position).getAlbumId()));
        if (imageUri != null) {
            Glide.with(context).load(imageUri).into(holder.albumArt);
        }
        if (holder.albumArt.getDrawable() == null) {
            holder.albumArt.setImageResource(R.drawable.msc_back1);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("sender", "AlbumDetails");
                intent.putExtra("position", position);
                SHOW_MINI_PLAYER = true;

                holder.equalizer.setVisibility(View.VISIBLE);
                if (!holder.equalizer.isAnimating())
                    holder.equalizer.animateBars();
                // holder.fileName.setTextColor(view.getResources().getColor(R.color.purple_200));
                holder.imageBackground.setBackgroundResource(R.color.purple_200);
                isPlaying = true;
                Activity activity = (Activity) context;
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.bottom_to_top, R.anim.top_to_bottom);
            }
        });

        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
                    @Override
                    public void onItemClick(int itemPosition, PowerMenuItem item) {
                        if (itemPosition == 0) {
                            deleteFile(position, view);
                        }
                    }
                };
                PowerMenu recycleMenu = new PowerMenu.Builder(context)
                        .setMenuColorResource(R.color.black)
                        .setTextColorResource(R.color.white)
                        .addItem(new PowerMenuItem("Delete", false))
                        .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                        .setMenuRadius(30f)
                        .setMenuShadow(10f)
                        .setSelectedEffect(false)
                        .setOnMenuItemClickListener(onMenuItemClickListener)
                        .setAutoDismiss(true)
                        .build();

                recycleMenu.showAsAnchorLeftTop(view);
            }
        });

    }


    /**
     * Удаление трека
     *
     * @param position позиция трека
     * @param view     View элемент
     */
    private void deleteFile(int position, View view) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(albumFiles.get(position).getId()));
        File file = new File(albumFiles.get(position).getPath());
        boolean delete = file.delete();
        if (delete) {
            if (musicServiceMain.mediaPlayer != null && musicServiceMain.isPlaying() && currentMusicPlaying.getId().equals(albumFiles.get(position).getId())) {
                musicServiceMain.stop();
                if (albumFiles.size() == 1) {
                    musicServiceMain.stop();
                    SHOW_MINI_PLAYER = false;
                    musicServiceMain.updateBottomPlayer.updatePlayer();
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(12312);
                } else {
                    musicServiceMain.updateBottomPlayer.btn_nextClicked();
                    musicServiceMain.updateBottomPlayer.updatePlayer();
                }
            }
            context.getContentResolver().delete(contentUri, null, null);
            if (musicFiles != null) {
                int indx = musicFiles.indexOf(albumFiles.get(position));
                if (indx != -1) {
                    musicFiles.remove(indx);
                    notifyItemRangeChanged(indx, musicFiles.size());
                }
            }
            if (albums != null) {
                int indx = albums.indexOf(albumFiles.get(position));
                if (indx != -1) {
                    albums.remove(indx);
                    notifyItemRangeChanged(indx, albums.size());
                }
            }
            albumFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, albumFiles.size());

            if (SongFragment.recyclerViewSong != null) {
                RecyclerView.Adapter songAdapter = SongFragment.recyclerViewSong.getAdapter();
                if (songAdapter != null) {
                    songAdapter.notifyDataSetChanged();
                }
            }
            if (AlbumFragment.recyclerViewAlbum != null) {
                RecyclerView.Adapter albumAdapter = AlbumFragment.recyclerViewAlbum.getAdapter();
                if (albumAdapter != null) {
                    albumAdapter.notifyDataSetChanged();
                }
            }
            Snackbar.make(view, "Deleted", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(view, "Can't be deleted", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * @return Размер списка
     */
    @Override
    public int getItemCount() {
        return albumFiles.size();
    }


    /**
     * Сам ViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView albumArt;//Shapeable
        final TextView fileName;
        final TextView artistName;
        final ImageView menuMore;
        ImageView imageBackground;
        public EqualizerView equalizer;

        ViewHolder(View view) {
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
