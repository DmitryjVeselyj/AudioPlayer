package com.polyap.music_player.song_fragment;


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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.polyap.music_player.player_activity.PlayerActivity;
import com.polyap.music_player.R;
import com.polyap.music_player.album_fragment.AlbumFragment;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.File;
import java.util.ArrayList;

import es.claucookie.miniequalizerlibrary.EqualizerView;
import me.zhanghai.android.fastscroll.PopupTextProvider;

//Адаптер для фрагмента
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> implements PopupTextProvider {
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    private final Context context;
    public static ArrayList<MusicFiles> musicFilesList;
    public static ViewHolder lastHolder;
    private int lastPosition = -1;
    public static CardView bottomPlayer;

    /**
     * Констуктор
     *
     * @param context        Контекст
     * @param musicFilesList список треков, которые будут отображаться
     */
    public MusicAdapter(Context context, ArrayList<MusicFiles> musicFilesList) {
        this.musicFilesList = musicFilesList;
        this.context = context;
    }

    /**
     * Создание ViewHolder-а
     *
     * @param parent   ViewGroup parent
     * @param viewType тип View
     * @return ViewHolder
     */
    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_items, parent, false);
        return new ViewHolder(view);
    }

    /**
     * очищаем анимации, когда надо и когда не надо
     *
     * @param holder Холдер
     */
    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.clearAnimation();
    }

    /**
     * В этом методе происходит "заполнение данными" каждого трека
     *
     * @param holder   ViewHolder
     * @param position позиция
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.recycleview_animation_down
                        : R.anim.recycleview_animation_up);
        holder.itemView.startAnimation(animation);
        lastPosition = position;
        if (currentMusicPlaying != null && musicFilesList.get(position).getId().equals(currentMusicPlaying.getId())) {
            lastHolder = holder;
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
        holder.fileName.setText(musicFilesList.get(position).getTitle());
        holder.artistName.setText(musicFilesList.get(position).getArtist());

        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(musicFilesList.get(position).getAlbumId()));
        if (imageUri != null) {
            Glide.with(context).load(imageUri).into(holder.albumArt);
        }
        if (holder.albumArt.getDrawable() == null)
            holder.albumArt.setImageResource(R.drawable.ic_o);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("sender", "MainActivity");
                intent.putExtra("position", position);
                SHOW_MINI_PLAYER = true;


                holder.equalizer.setVisibility(View.VISIBLE);
                if (!holder.equalizer.isAnimating())
                    holder.equalizer.animateBars();
                // holder.fileName.setTextColor(view.getResources().getColor(R.color.purple_200));
                holder.imageBackground.setBackgroundResource(R.color.purple_200);

                Activity activity = (Activity) context;
                isPlaying = true;
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
                Long.parseLong(musicFilesList.get(position).getId()));
        File file = new File(musicFilesList.get(position).getPath());
        boolean delete = file.delete();
        if (delete) {
            if (musicServiceMain.mediaPlayer != null && musicServiceMain.isPlaying() && currentMusicPlaying.getId().equals(musicFilesList.get(position).getId())) {
                musicServiceMain.stop();
                if (musicFilesList.size() == 1) {
                    musicServiceMain.stop();
                    SHOW_MINI_PLAYER = false;
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(12312);
                    musicServiceMain.updateBottomPlayer.updatePlayer();
                } else {
                    musicServiceMain.updateBottomPlayer.btn_nextClicked();
                    musicServiceMain.updateBottomPlayer.updatePlayer();
                }
            }
            context.getContentResolver().delete(contentUri, null, null);
            if (albums != null) {
                int indx = albums.indexOf(musicFiles.get(position));
                if (indx != -1) {
                    albums.remove(indx);
                    notifyItemRangeChanged(indx, albums.size());
                }
            }
            musicFilesList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, musicFilesList.size());

            if (AlbumFragment.recyclerViewAlbum != null) {
                RecyclerView.Adapter albumAdapter = AlbumFragment.recyclerViewAlbum.getAdapter();
                if (albumAdapter != null) {
                    albumAdapter.notifyDataSetChanged();//переписать для одного элемента, дабы все не обновлять
                }
            }
            Snackbar.make(view, "Deleted", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(view, "Can't be deleted ", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * @return Размер списка
     */
    @Override
    public int getItemCount() {
        return musicFilesList.size();
    }


    /**
     * Метод, используемый для скроллбара
     *
     * @param position позиция
     * @return первый символ строки названия альбома
     */
    @NonNull
    @Override
    public String getPopupText(int position) {
        String title = musicFilesList.get(position).getTitle();
        return String.valueOf(title.charAt(0));
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
        ConstraintLayout musicLayout;

        ViewHolder(View view) {
            super(view);
            fileName = view.findViewById(R.id.music_file_name);
            albumArt = view.findViewById(R.id.music_img);
            artistName = view.findViewById(R.id.recycle_view_artist);
            menuMore = view.findViewById(R.id.menu_more);
            equalizer = (EqualizerView) view.findViewById(R.id.equalizer_view);
            musicLayout = view.findViewById(R.id.music_item_layout);
            imageBackground = view.findViewById(R.id.image_background);
            //bottomPlayer = view.findViewById(R.id.frag_bottom_player);

        }

        public void clearAnimation() {
            musicLayout.clearAnimation();
        }
    }

    /**
     * Обновление списка треков
     *
     * @param musicFilesArrayList новый список треков
     */
    public void updateList(ArrayList<MusicFiles> musicFilesArrayList) {
        musicFilesList = new ArrayList<>();
        musicFilesList.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}

