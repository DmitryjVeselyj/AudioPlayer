package com.polyap.music_player.bottom_player;

import static android.content.Context.MODE_PRIVATE;
import static com.polyap.music_player.album_details.AlbumDetailsAdapter.lastAlbumHolder;
import static com.polyap.music_player.main_activity.MainActivity.SHOW_MINI_PLAYER;
import static com.polyap.music_player.main_activity.MainActivity.currentMusicPlaying;
import static com.polyap.music_player.main_activity.MainActivity.isRepeat;
import static com.polyap.music_player.main_activity.MainActivity.lastMusicPosition;
import static com.polyap.music_player.main_activity.MainActivity.lastMusicQueue;
import static com.polyap.music_player.main_activity.MainActivity.musicServiceMain;
import static com.polyap.music_player.main_activity.MainActivity.oldMusicPlayed;
import static com.polyap.music_player.music_service.MusicService.MUSIC_FILE;
import static com.polyap.music_player.music_service.MusicService.MUSIC_LAST_PLAYED;
import static com.polyap.music_player.song_fragment.MusicAdapter.lastHolder;
import static com.polyap.music_player.player_activity.PlayerActivity.BACK;
import static com.polyap.music_player.player_activity.PlayerActivity.FORWARD;
import static com.polyap.music_player.player_activity.PlayerActivity.fragment;
import static com.polyap.music_player.player_activity.PlayerActivity.isChangedMusic;
import static com.polyap.music_player.player_activity.PlayerActivity.isPlaying;
import static com.polyap.music_player.player_activity.PlayerActivity.updateSongList;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.palette.graphics.Palette;

import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.polyap.music_player.player_activity.PlayerActivity;
import com.polyap.music_player.R;
import com.polyap.music_player.interfaces.UpdateBottomPlayer;
import com.polyap.music_player.music_service.MusicService;
import com.polyap.music_player.object_serializer.ObjectSerializer;
import com.polyap.music_player.song_fragment.MusicFiles;
import com.polyap.po_equalizer.DialogEqualizerFragment;

import java.io.IOException;
import java.util.ArrayList;

/**
 * класс, отвечающий за мини-плеер
 */
public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection, UpdateBottomPlayer {

    ImageView nextBtn, albumArt, prevBtn;
    TextView artist, songName;
    ImageView playPauseBtn;
    public static View view;//мб под статики сделать
    public static MusicService musicServiceFrag;
    Uri uri;

    public static ColorDrawable lastColorBottom = new ColorDrawable(Color.BLACK);
    public static int plPosition;
    public boolean init = false;
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");

    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }

    /**
     * метод Fragment, вызывается при создании фрагмента
     *
     * @param savedInstanceState сохранённое состояние
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initService();
        musicServiceMain = musicServiceFrag;

    }

    /**
     * Инициализация
     *
     * @param inflater           экземляр класса, создающего из layout-файла View-элемент
     * @param container          ViewGroup контейнер
     * @param savedInstanceState сохранённое состояние
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);
        artist = view.findViewById(R.id.song_artist_miniPlayer);
        songName = view.findViewById(R.id.song_name_miniPlayer);
        albumArt = view.findViewById(R.id.bottom_album_art);
        nextBtn = view.findViewById(R.id.skip_next_bottom);
        prevBtn = view.findViewById(R.id.skip_prev_bottom);
        playPauseBtn = view.findViewById(R.id.play_pause_miniPlayer);
        songName.setSelected(true);
        artist.setSelected(true);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicServiceFrag != null) {
                    playMusic(getNewPosition(FORWARD));
                    updatePlayer();

                }
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicServiceFrag != null) {
                    playMusic(getNewPosition(BACK));
                    updatePlayer();

                }
            }
        });
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicServiceFrag != null) {
                    isPlaying = !isPlaying;
                    if (musicServiceFrag.mediaPlayer != null) {
                        if (musicServiceFrag.isPlaying()) {
                            musicServiceFrag.pause();
                            isPlaying = false;
                            musicServiceFrag.showNotification(R.drawable.ic_play_n, 0f, PlaybackStateCompat.STATE_PLAYING);
                            playPauseBtn.setImageResource(R.drawable.ic_play_n);

                        } else {
                            musicServiceFrag.start();
                            isPlaying = true;
                            playPauseBtn.setImageResource(R.drawable.ic_pause_n);
                            musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PLAYING);

                        }
                        updateCurrentSong();
                    } else {
                        playMusic(plPosition);
                        updatePlayer();
                    }

                }

            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PlayerActivity.class);
                intent.putExtra("sender", "BottomPlayer");
                Activity activity = (Activity) getContext();
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.bottom_to_top, R.anim.top_to_bottom);

            }
        });
        if (SHOW_MINI_PLAYER == false)
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
        return view;
    }

    /**
     * обновление текущего трека(анимация)
     */
    public static void updateCurrentSong() {

        if (lastHolder != null) {
            if (isPlaying) {
                lastHolder.equalizer.animateBars();
            } else {
                lastHolder.equalizer.stopBars();
            }
        }
        if (lastAlbumHolder != null) {
            if (isPlaying) {
                lastAlbumHolder.equalizer.animateBars();
            } else {
                lastAlbumHolder.equalizer.stopBars();
            }
        }
    }

    /**
     * Вызывается при "восстановлении" фрагментп
     */
    @Override
    public void onResume() {
        super.onResume();
        if (SHOW_MINI_PLAYER) {

            updatePlayer();
            plPosition = lastMusicPosition;
            Intent intent = new Intent(getContext(), MusicService.class);
            if (getContext() != null) {
                getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
            }
        }
    }


    /**
     * Как только сервис подключился, инициализируем элементы
     *
     * @param componentName имя компонента
     * @param iBinder       элемент, реализующий интерфейс IBinder
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder binder = (MusicService.MyBinder) iBinder;
        musicServiceFrag = binder.getService();
        plPosition = lastMusicPosition;
        musicServiceFrag.setCallBack1(this);
        musicServiceMain = musicServiceFrag;
    }

    /**
     * Метод вызывается, если сервис отключился
     *
     * @param componentName имя компонента
     */
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicServiceFrag = null;
    }


    /**
     * Запуск проигрывания музыки
     *
     * @param position позиция трека
     */
    void playMusic(int position) {
        plPosition = position;
        PlayerActivity.position = position;
        musicServiceFrag.setPosition(position);
        if (lastMusicQueue != null) {
            uri = Uri.parse(lastMusicQueue.get(position).getPath());
        }
        if (musicServiceFrag.mediaPlayer != null) {
            musicServiceFrag.stop();
            musicServiceFrag.reset();

            try {
                musicServiceFrag.setDataSource(uri);
                musicServiceFrag.prepare();

            } catch (IOException e) {
                e.printStackTrace();

            }

        } else {
            musicServiceFrag.createMediaPlayer(position);
            musicServiceFrag.OnCompleted();
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        }
        if (isPlaying) {
            musicServiceFrag.start();
            musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PLAYING);
        } else {
            musicServiceFrag.showNotification(R.drawable.ic_play_n, 0f, PlaybackStateCompat.STATE_PLAYING);
        }

        oldMusicPlayed = currentMusicPlaying;
        currentMusicPlaying = lastMusicQueue.get(position);
        int audioSessionId = musicServiceFrag.getAudioSessionId();
        if (currentMusicPlaying.equals(oldMusicPlayed) || oldMusicPlayed == null)
            isChangedMusic = true;
        else
            isChangedMusic = true;
        updateSongList();
        if (fragment != null && getContext() != null) {
            fragment = DialogEqualizerFragment.newBuilder()
                    .setAudioSessionId(audioSessionId)
                    .themeColor(ContextCompat.getColor(getContext(), R.color.black))
                    .textColor(ContextCompat.getColor(getContext(), R.color.white))
                    .accentAlpha(ContextCompat.getColor(getContext(), R.color.purple_200))
                    .darkColor(ContextCompat.getColor(getContext(), R.color.purple_200))
                    .setAccentColor(ContextCompat.getColor(getContext(), R.color.purple_200))
                    .build();
        } else if (getContext() != null) {
            fragment = DialogEqualizerFragment.newBuilder()
                    .setAudioSessionId(audioSessionId)
                    .themeColor(ContextCompat.getColor(getContext(), R.color.black))
                    .textColor(ContextCompat.getColor(getContext(), R.color.white))
                    .accentAlpha(ContextCompat.getColor(getContext(), R.color.purple_200))
                    .darkColor(ContextCompat.getColor(getContext(), R.color.purple_200))
                    .setAccentColor(ContextCompat.getColor(getContext(), R.color.purple_200))
                    .build();
            equalizerUpdate();
        }


        lastMusicPosition = position;
        musicServiceFrag.saveLastTrack(position);
        //updatePlayer();
        init = true;
    }

    //Вы этого не видели
    private void equalizerUpdate() {
        FragmentManager fm = getActivity().getSupportFragmentManager();//дичайший костыль. Еле держится, но работает
        fm.beginTransaction().hide(fragment).commit();
        fragment.show(getActivity().getSupportFragmentManager(), "eq");
        fm.beginTransaction().remove(fragment).commit();
    }

    /**
     * Метод, возращающий позицию нового трека
     *
     * @param direction направление
     * @return позиция нового трека для запуска
     */
    private int getNewPosition(String direction) {
        if (isRepeat)
            return plPosition;
        if (direction.equals(FORWARD)) {
            return (plPosition + 1) % lastMusicQueue.size();
        } else {
            return (plPosition - 1) < 0 ? (lastMusicQueue.size()) - 1 : plPosition - 1;
        }
    }

    /**
     * Инициализация сервиса
     */
    void initService() {
        if (musicServiceFrag == null) {
            Intent intent = new Intent(getContext(), MusicService.class);
            intent.putExtra("servicePosition", plPosition);
            getContext().startService(intent);
        }
    }

    /**
     * Плавное изменение цвета мини-плеера в зависимости от трека
     */
    private void changeBackColor() {
        Bitmap bitmap = ((BitmapDrawable) albumArt.getDrawable()).getBitmap();
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                Palette.Swatch swatch = palette.getDominantSwatch();
                if (swatch != null) {
                    float[] hsl = swatch.getHsl();
                    hsl[1] = hsl[1] > (float) 0.5 ? (float) 0.31 : hsl[1];
                    hsl[2] = hsl[2] < (float) 0.5 ? (float) 0.34 : hsl[2];
                    hsl[2] = hsl[2] > (float) 0.83 ? (float) 0.3 : hsl[2];
                    int color = ColorUtils.HSLToColor(hsl);
                    ColorDrawable colorDrawable1 = new ColorDrawable(color);
                    ColorDrawable[] cd = {lastColorBottom, colorDrawable1};
                    TransitionDrawable transitionDrawable = new TransitionDrawable(cd);
                    view.setBackground(transitionDrawable);

                    transitionDrawable.startTransition(400);
                    lastColorBottom = colorDrawable1;

                }
            }
        });
    }


    /**
     * Реализация метода интерфейса
     */
    @Override
    public void updatePlayer() {
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(lastMusicQueue.get(lastMusicPosition).getAlbumId()));
        albumArt.setImageURI(imageUri);
        if (albumArt.getDrawable() == null) {
            albumArt.setImageResource(R.drawable.msc_back1);
        }
        songName.setText(lastMusicQueue.get(lastMusicPosition).getTitle());
        artist.setText(lastMusicQueue.get(lastMusicPosition).getArtist());
        changeBackColor();
        if (isPlaying) {
            playPauseBtn.setImageResource(R.drawable.ic_pause_n);
            isPlaying = true;
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_play_n);
            isPlaying = false;
        }
        if (SHOW_MINI_PLAYER == false)
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
    }

    @Override
    /**
     * Реализация метода интерфейса
     */
    public void btn_play_pauseClicked() {
        if (musicServiceFrag.isPlaying()) {
            musicServiceFrag.pause();
            isPlaying = false;
            musicServiceFrag.showNotification(R.drawable.ic_play_n, 0f, PlaybackStateCompat.STATE_PLAYING);
        } else {
            musicServiceFrag.start();
            isPlaying = true;
            musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PLAYING);

        }
        updateCurrentSong();
    }

    @Override
    /**
     * Реализация метода интерфейса
     */
    public void btn_nextClicked() {
        musicServiceFrag.mediaPlayer.seekTo(0);
        if (isPlaying)
            musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PAUSED);
        else
            musicServiceFrag.showNotification(R.drawable.ic_play_n, 1f, PlaybackStateCompat.STATE_PAUSED);
        playMusic(getNewPosition(FORWARD));
    }

    @Override
    /**
     * Реализация метода интерфейса
     */
    public void btn_prevClicked() {
        musicServiceFrag.mediaPlayer.seekTo(0);
        if (isPlaying)
            musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PAUSED);//1f state playing
        else
            musicServiceFrag.showNotification(R.drawable.ic_play_n, 1f, PlaybackStateCompat.STATE_PAUSED);//0f
        playMusic(getNewPosition(BACK));
    }

    @Override
    /**
     * Реализация метода интерфейса
     */
    public void btn_dismiss() {
        Activity activity = getActivity();
        if (activity != null && activity.getBaseContext() != null) {
            NotificationManager mNotificationManager = (NotificationManager) activity.getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(12312);
        }

        if (musicServiceFrag != null) {
            musicServiceFrag.pause();
            musicServiceFrag.deleteNot();
            isPlaying = false;
            playPauseBtn.setImageResource(R.drawable.ic_play_n);
        }
        updateCurrentSong();
    }

    /**
     * Связываем фрагмент с активити
     *
     * @param activity Активити
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

}