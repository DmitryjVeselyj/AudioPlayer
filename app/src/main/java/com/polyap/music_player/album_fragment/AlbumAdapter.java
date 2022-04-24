package com.polyap.music_player.album_fragment;

import static com.polyap.music_player.main_activity.MainActivity.musicFiles;
import static com.polyap.music_player.player_activity.PlayerActivity.getPosition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.polyap.music_player.R;
import com.polyap.music_player.album_details.AlbumDetails;
import com.polyap.music_player.song_fragment.MusicFiles;

import java.util.ArrayList;

import me.zhanghai.android.fastscroll.PopupTextProvider;


/**
 * Адаптер для фрагмента "альбомы"
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> implements PopupTextProvider {
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    private Context context;
    public static ArrayList<MusicFiles> albumFilesFragment;
    private int lastPosition = -1;
    View view;

    /**
     * Констуктор
     *
     * @param context    Контекст
     * @param albumFiles список треков, альбомы которых будут отображаться
     */
    public AlbumAdapter(Context context, ArrayList<MusicFiles> albumFiles) {
        this.context = context;
        this.albumFilesFragment = albumFiles;
    }

    /**
     * Создание ViewHolder-а
     *
     * @param parent   ViewGroup parent
     * @param viewType тип View
     * @return ViewHolder
     */
    @NonNull
    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);

        return new ViewHolder(view);
    }

    /**
     * В этом методе происходит "заполнение данными" каждого альбома
     *
     * @param holder   ViewHolder
     * @param position позиция
     */
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
        if (imageUri != null) {
            Glide.with(context).load(imageUri).into(holder.albumImage);
        }
        if (holder.albumImage.getDrawable() == null)
            holder.albumImage.setImageResource(R.drawable.msc_back1);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AlbumDetails.class);
                intent.putExtra("albumName", albumFilesFragment.get(position).getAlbum())
                        .putExtra("artistName", albumFilesFragment.get(position).getArtist());
                intent.putExtra("position", getPosition((ArrayList<MusicFiles>) musicFiles, albumFilesFragment.get(position)));
                // ActivityOptions options =
                // ActivityOptions.makeCustomAnimation(context, R.anim.bottom_to_top, R.anim.recycleview_animation_up );
                //context.startActivity(intent);

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity) context, holder.albumImage, "art");
                context.startActivity(intent, options.toBundle());


            }
        });
    }


    /**
     * @return размер списка альбомов
     */
    @Override
    public int getItemCount() {
        return albumFilesFragment.size();
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
        String albumN = albumFilesFragment.get(position).getAlbum();
        return String.valueOf(albumN.charAt(0));
    }

    /**
     * ViewHilder класс
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView albumImage;
        TextView albumName;
        TextView albumArtist;

        public ViewHolder(View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.album_image);
            albumName = itemView.findViewById(R.id.album_name);
            albumArtist = itemView.findViewById(R.id.album_artist);
        }
    }

    /**
     * Обновление списка альбомов
     *
     * @param albumList новый список треков, по которым строятся альбомы
     */
    public void updateList(ArrayList<MusicFiles> albumList) {
        albumFilesFragment = new ArrayList<>();
        albumFilesFragment.addAll(albumList);
        notifyDataSetChanged();
    }

}
