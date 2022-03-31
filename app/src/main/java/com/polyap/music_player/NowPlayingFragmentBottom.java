package com.polyap.music_player;

import static android.content.Context.MODE_PRIVATE;
import static com.polyap.music_player.AlbumDetailsAdapter.albumFiles;
import static com.polyap.music_player.AlbumDetailsAdapter.lastAlbumHolder;
import static com.polyap.music_player.MainActivity.SHOW_MINI_PLAYER;
import static com.polyap.music_player.MainActivity.currentMusicPlaying;
import static com.polyap.music_player.MainActivity.isRepeat;
import static com.polyap.music_player.MainActivity.lastMusicPosition;
import static com.polyap.music_player.MainActivity.lastMusicQueue;
import static com.polyap.music_player.MainActivity.musicServiceMain;
import static com.polyap.music_player.MainActivity.oldMusicPlayed;
import static com.polyap.music_player.MusicAdapter.lastHolder;
import static com.polyap.music_player.MusicAdapter.musicFilesList;
import static com.polyap.music_player.MusicService.MUSIC_FILE;
import static com.polyap.music_player.MusicService.MUSIC_LAST_PLAYED;
import static com.polyap.music_player.PlayerActivity.BACK;
import static com.polyap.music_player.PlayerActivity.FORWARD;
import static com.polyap.music_player.PlayerActivity.fragment;
import static com.polyap.music_player.PlayerActivity.getPosition;
import static com.polyap.music_player.PlayerActivity.isChangedMusic;
import static com.polyap.music_player.PlayerActivity.isPlaying;
import static com.polyap.music_player.PlayerActivity.updateSongList;
import static com.polyap.music_player.SongFragment.musicAdapter;

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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.polyap.po_equalizer.DialogEqualizerFragment;

import java.io.IOException;
import java.util.ArrayList;

public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection, UpdateBottomPlayer {

    ImageView nextBtn, albumArt, prevBtn;
    TextView artist, songName;
    ImageView playPauseBtn;
    public static View view;//мб под статики сделать
    static MusicService musicServiceFrag;
    Uri uri;

    public static ColorDrawable lastColorBottom = new ColorDrawable(Color.BLACK);
    public static int plPosition;
    public boolean init = false;
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");

    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initService();
        musicServiceMain = musicServiceFrag;

    }

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
                if(musicServiceFrag != null){
                    playMusic(getNewPosition(FORWARD));
                    updatePlayer();

                }
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicServiceFrag != null){
                    playMusic(getNewPosition(BACK));
                    updatePlayer();

                }
            }
        });
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicServiceFrag != null) {
                    isPlaying = !isPlaying;
                    if(musicServiceFrag.mediaPlayer != null){
                        if(musicServiceFrag.isPlaying()){
                            musicServiceFrag.pause();
                            isPlaying = false;
                            musicServiceFrag.showNotification(R.drawable.ic_play_n, 0f,  PlaybackStateCompat.STATE_PLAYING);
                            playPauseBtn.setImageResource(R.drawable.ic_play_n);

                        }
                        else{
                            musicServiceFrag.start();
                            isPlaying = true;
                            playPauseBtn.setImageResource(R.drawable.ic_pause_n);
                            musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f,  PlaybackStateCompat.STATE_PLAYING);

                        }
                        updateCurrentSong();
                    }
                    else{
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
                Activity activity = (Activity)getContext();
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.bottom_to_top, R.anim.top_to_bottom);

            }
        });
        if(SHOW_MINI_PLAYER == false)
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
        return view;
    }
    static void updateCurrentSong(){
        /*if (SongFragment.recyclerViewSong != null) {
            RecyclerView.Adapter songAdapter = SongFragment.recyclerViewSong.getAdapter();
            if (songAdapter != null) {
                if(currentMusicPlaying != null) {
                    songAdapter.notifyItemChanged(getPosition((ArrayList<MusicFiles>) musicFilesList, currentMusicPlaying));
                }
            }
        }

        if(AlbumDetails.recyclerView != null){
            RecyclerView.Adapter albumDetailsAdapter = AlbumDetails.recyclerView.getAdapter();
            if(albumDetailsAdapter != null){
                if(currentMusicPlaying != null)
                    albumDetailsAdapter.notifyItemChanged(getPosition((ArrayList<MusicFiles>) albumFiles, currentMusicPlaying));
            }
        }
        isChangedMusic = false;*/
        if(lastHolder != null) {
            if (isPlaying) {
                lastHolder.equalizer.animateBars();
            } else {
                lastHolder.equalizer.stopBars();
            }
        }
        if(lastAlbumHolder != null){
            if (isPlaying) {
                lastAlbumHolder.equalizer.animateBars();
            } else {
                lastAlbumHolder.equalizer.stopBars();
            }
        }
    }
    void saveLastTrack(int position){
        if(getActivity()!= null) {
            SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
            ArrayList<MusicFiles> tmp = new ArrayList<>();
            tmp.add(lastMusicQueue.get(position));
            try {
                editor.putString(MUSIC_FILE, ObjectSerializer.serialize(tmp));
            } catch (IOException e) {
                e.printStackTrace();
            }
            editor.apply();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(SHOW_MINI_PLAYER){

            updatePlayer();
            plPosition = lastMusicPosition;
            Intent intent = new Intent(getContext(), MusicService.class);
            if(getContext()!= null){
                getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
            }
        }
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder binder = (MusicService.MyBinder) iBinder;
        musicServiceFrag = binder.getService();
        plPosition = lastMusicPosition;
        musicServiceFrag.setCallBack1(this);
        musicServiceMain = musicServiceFrag;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicServiceFrag = null;
    }

    void nextBtnClick(){
        musicServiceFrag.mediaPlayer.seekTo(0);
        musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PAUSED);
    }
    void playMusic(int position){
        plPosition = position;
        PlayerActivity.position = position;
        musicServiceFrag.setPosition(position);
        if(lastMusicQueue != null){
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
            musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f,  PlaybackStateCompat.STATE_PLAYING);
        } else {
            musicServiceFrag.showNotification(R.drawable.ic_play_n, 0f,  PlaybackStateCompat.STATE_PLAYING);
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
        } else if (getContext() != null){
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
    private void equalizerUpdate(){
        FragmentManager fm = getActivity().getSupportFragmentManager();//дичайший костыль. Еле держится, но работает
        fm.beginTransaction().hide(fragment).commit();
        fragment.show(getActivity().getSupportFragmentManager(), "eq");
        fm.beginTransaction().remove(fragment).commit();
    }

    private int getNewPosition(String direction){
        if(isRepeat)
            return plPosition;
        if(direction.equals(FORWARD)){
            return (plPosition + 1) % lastMusicQueue.size();
        }
        else{
            return (plPosition - 1) < 0 ? (lastMusicQueue.size()) - 1: plPosition - 1;
        }
    }
    void initService(){
        if(musicServiceFrag == null) {
            Intent intent = new Intent(getContext(), MusicService.class);
            intent.putExtra("servicePosition", plPosition);
            getContext().startService(intent);
        }
    }

    private void changeBackColor(){
        Bitmap bitmap = ((BitmapDrawable)albumArt.getDrawable()).getBitmap();
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                Palette.Swatch swatch = palette.getDominantSwatch();
                if(swatch != null){
                    float[] hsl = swatch.getHsl();
                    hsl[1] = hsl[1] > (float) 0.5? (float)0.31 : hsl[1];
                    hsl[2] = hsl[2] < (float) 0.5? (float)0.34 : hsl[2];
                    hsl[2] = hsl[2] > (float) 0.83? (float)0.3:hsl[2];//поменять когда-то
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }



    @Override
    public void updatePlayer() {
            Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(lastMusicQueue.get(lastMusicPosition).getAlbumId()));
            albumArt.setImageURI(imageUri);
            if(albumArt.getDrawable() == null){
                albumArt.setImageResource(R.drawable.msc_back1);
            }
            songName.setText(lastMusicQueue.get(lastMusicPosition).getTitle());
            artist.setText(lastMusicQueue.get(lastMusicPosition).getArtist());
            changeBackColor();
            if(isPlaying){
                playPauseBtn.setImageResource(R.drawable.ic_pause_n);
                isPlaying = true;
            }
            else{
                playPauseBtn.setImageResource(R.drawable.ic_play_n);
                isPlaying = false;
            }
        if(SHOW_MINI_PLAYER == false)
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
    }

    @Override
    public void btn_play_pauseClicked() {
        if(musicServiceFrag.isPlaying()){
            musicServiceFrag.pause();
            isPlaying = false;
            musicServiceFrag.showNotification(R.drawable.ic_play_n, 0f,  PlaybackStateCompat.STATE_PLAYING);
        }
        else{
            musicServiceFrag.start();
            isPlaying = true;
            musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f,  PlaybackStateCompat.STATE_PLAYING);

        }
        updateCurrentSong();
    }

    @Override
    public void btn_nextClicked() {
        musicServiceFrag.mediaPlayer.seekTo(0);
        if(isPlaying)
            musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PAUSED);
        else
            musicServiceFrag.showNotification(R.drawable.ic_play_n, 1f, PlaybackStateCompat.STATE_PAUSED);
        playMusic(getNewPosition(FORWARD));
    }

    @Override
    public void btn_prevClicked() {
        musicServiceFrag.mediaPlayer.seekTo(0);
        if(isPlaying)
            musicServiceFrag.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PAUSED);
        else
            musicServiceFrag.showNotification(R.drawable.ic_play_n, 1f, PlaybackStateCompat.STATE_PAUSED);
        playMusic(getNewPosition(BACK));
    }

    @Override
    public void btn_dismiss() {
        Activity activity = getActivity();
        if(activity != null && activity.getBaseContext() != null) {
            NotificationManager mNotificationManager = (NotificationManager) activity.getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(12312);
        }

        if(musicServiceFrag != null){
            musicServiceFrag.pause();
            musicServiceFrag.deleteNot();
            isPlaying = false;
            playPauseBtn.setImageResource(R.drawable.ic_play_n);
        }
        updateCurrentSong();
    }

    @Override
    public void btn_start() {
        playMusic(plPosition);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

}