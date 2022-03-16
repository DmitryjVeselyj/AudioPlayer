package com.polyap.music_player;

import static android.graphics.BlendMode.COLOR;
import static com.polyap.music_player.MainActivity.isRepeat;
import static com.polyap.music_player.MainActivity.isShuffle;
import static com.polyap.music_player.MainActivity.isVisualize;
import static com.polyap.music_player.MainActivity.musicFiles;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import com.gauravk.audiovisualizer.base.BaseVisualizer;
import com.gauravk.audiovisualizer.visualizer.WaveVisualizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.masoudss.lib.SeekBarOnProgressChanged;
import com.masoudss.lib.WaveformSeekBar;
import com.polyap.po_equalizer.DialogEqualizerFragment;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation;


public class PlayerActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    TextView songName, artistName, durationPlayed, durationTotal;
    ImageView albumArt, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn, menuBtn;
    RelativeLayout playerLayout;
    FloatingActionButton playpauseBtn;
    WaveVisualizer visualizer;
    ExecutorService service = Executors.newFixedThreadPool(1);
    boolean isPlaying = true;
    private boolean init = false;
    int position = -1;
    static ArrayList<MusicFiles> listSongs;
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private int oldAudioSessionId = -2;
    final static String FORWARD = "forward";
    final static String BACK = "back";
    String direction = FORWARD;
    WaveformSeekBar waveformSeekBar;
    ColorDrawable lastColor = new ColorDrawable(Color.BLACK);
    boolean shouldClick = false;
    DialogEqualizerFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();

        InitViews();
        position = getIntent().getIntExtra("position", -1);
        if(!isShuffle){
            listSongs = (ArrayList<MusicFiles>) musicFiles.clone();
        }else{
            MusicFiles song = musicFiles.get(position);
            listSongs.remove(song);
            Collections.shuffle(listSongs);
            listSongs.add(position, song);
        }
        getIntentMethod(position);
        init = true;

        mainListeners();



    }

    @Override
    public void onBackPressed() {
        visualizer.release();
        super.onBackPressed();
    }

    private String formattedTime(int currentPosition){
        String totalout = "";
        String totalnew = "";
        String seconds = String.valueOf(currentPosition % 60);
        String minutes = String.valueOf(currentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalnew = minutes + ":" + "0" + seconds;
        if(seconds.length() == 1){
            return totalnew;
        }
        else{
            return totalout;
        }
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null)
            visualizer.setEnabled(false);
            visualizer.release();
        super.onDestroy();
        service.shutdown();

    }


    private int getNewPosition(String direction){
        if(direction.equals(FORWARD)){
            return (position + 1) % listSongs.size();
        }
        else{
            return (position - 1) < 0 ? (listSongs.size()) - 1: position - 1;
        }
    }

    private void mainListeners(){

        waveformSeekBar.setOnProgressChanged(new SeekBarOnProgressChanged() {
            @Override
            public void onProgressChanged(@NonNull WaveformSeekBar waveformSeekBar, float v, boolean b) {
                durationPlayed.setText(formattedTime((int) waveformSeekBar.getProgress()));
            }
        });

       PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    waveformSeekBar.setProgress(currentPosition);
                    durationPlayed.setText(formattedTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(isRepeat)
                    getIntentMethod(position);
                else
                    getIntentMethod(getNewPosition(FORWARD));
            }
        });
    }

    private void getIntentMethod(int position) {
        this.position = position;

        if(listSongs != null){
            uri = Uri.parse(listSongs.get(position).getPath());
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.waveform_animation);
            waveformSeekBar.startAnimation(animation);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    waveformSeekBar.setSampleFrom(uri.toString());

                }
            };
            service.execute(runnable);
            loadImages(position);

        }
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(getApplicationContext(), uri);
                mediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        }


        if(isPlaying || !init) {
            playpauseBtn.setImageResource(R.drawable.ic_pause);
            albumArt.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300);

            mediaPlayer.start();
        }
        else{
            playpauseBtn.setImageResource(R.drawable.ic_play);
        }

        waveformSeekBar.setProgress(0);
        waveformSeekBar.setMaxProgress(mediaPlayer.getDuration() / 1000);

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1 && oldAudioSessionId != audioSessionId) {
            visualizer.setAudioSessionId(audioSessionId);
            oldAudioSessionId = audioSessionId;
            fragment = DialogEqualizerFragment.newBuilder()
                    .setAudioSessionId(audioSessionId)
                    .themeColor(ContextCompat.getColor(this, R.color.black))
                    .textColor(ContextCompat.getColor(this, R.color.white))
                    .accentAlpha(ContextCompat.getColor(this, R.color.purple_200))
                    .darkColor(ContextCompat.getColor(this, R.color.purple_200))
                    .setAccentColor(ContextCompat.getColor(this, R.color.purple_200))
                    .build();

        }



    }

    private void InitViews() {
        songName = findViewById(R.id.song_name);
        songName.setSelected(true);
        artistName = findViewById(R.id.song_artist);
        artistName.setSelected(true);

        durationPlayed = findViewById(R.id.duration_played);
        durationTotal = findViewById(R.id.duration_total);

        albumArt = findViewById(R.id.album_art);
        prevBtn = findViewById(R.id.skip_prev);
        nextBtn = findViewById(R.id.skip_next);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.shuffle);
        repeatBtn = findViewById(R.id.repeat);
        playpauseBtn = findViewById(R.id.play_pause);
        menuBtn = findViewById(R.id.player_menu);

        visualizer = findViewById(R.id.wave_visualizer);
        waveformSeekBar = findViewById(R.id.waveformSeekBar);
        waveformSeekBar.setOnTouchListener(this);
        playerLayout = findViewById(R.id.player_activity);
        nextBtn.setOnTouchListener(this);
        nextBtn.setOnClickListener(this);

        playpauseBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        prevBtn.setOnTouchListener(this);
        backBtn.setOnClickListener(this);
        shuffleBtn.setOnClickListener(this);
        repeatBtn.setOnClickListener(this);
        menuBtn.setOnClickListener(this);

        playpauseBtn.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50);
        nextBtn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50);
        prevBtn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50);
        shuffleBtn.animate().scaleX(0.8f).scaleY(0.8f).setDuration(50);
        repeatBtn.animate().scaleX(0.8f).scaleY(0.8f).setDuration(50);
    }
    private void changeVisualizerColor(BaseVisualizer visualizer){
        Bitmap bitmap = ((BitmapDrawable)albumArt.getDrawable()).getBitmap();
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                Palette.Swatch swatch = palette.getDominantSwatch();
                if(swatch != null){
                    float[] hsl = swatch.getHsl();
                    hsl[1] = hsl[1] > (float) 0.5? (float)0.31 : hsl[1];
                    hsl[2] = hsl[2] < (float) 0.5? (float)0.34 : hsl[2];
                    int color = ColorUtils.HSLToColor(hsl);
                    visualizer.setColor(color);
                    waveformSeekBar.setWaveProgressColor(color);
                    ColorDrawable colorDrawable1 = new ColorDrawable(color);
                    ColorDrawable[] cd = {lastColor, colorDrawable1};
                    TransitionDrawable transitionDrawable = new TransitionDrawable(cd);
                    playerLayout.setBackground(transitionDrawable);
                    transitionDrawable.startTransition(400);
                    lastColor = colorDrawable1;
                }
            }
        });
    }
    private void loadImages(int position){

        final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(listSongs.get(position).getAlbumId()));
        Animation animationFirst;
        Animation animationSecond;
        if(direction.equals(FORWARD)) {
            animationFirst = AnimationUtils.loadAnimation(this, R.anim.slide_left);
            animationSecond = AnimationUtils.loadAnimation(this, R.anim.slide_left_sec);
        }
        else{
            animationFirst = AnimationUtils.loadAnimation(this, R.anim.slide_right);
            animationSecond = AnimationUtils.loadAnimation(this, R.anim.slide_right_sec);
        }
        animationFirst.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                albumArt.startAnimation(animationSecond);
                if(imageUri != null){
                    albumArt.setImageURI(imageUri);
                }
                if(albumArt.getDrawable() == null) {
                    albumArt.setImageResource(R.drawable.msc_back);
                }
                changeVisualizerColor(visualizer);
                songName.setText(listSongs.get(position).getTitle());
                artistName.setText(listSongs.get(position).getArtist());
                durationTotal.setText(formattedTime(Integer.parseInt(listSongs.get(position).getDuration()) / 1000));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        albumArt.startAnimation(animationFirst);


        if(isRepeat){
            repeatBtn.setImageResource(R.drawable.ic_repeat_on);
        }else{
            repeatBtn.setImageResource(R.drawable.ic_repeat);
        }

        if(isShuffle){
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);;
        }else{
            shuffleBtn.setImageResource(R.drawable.ic_shuffle);
        }

        if(isVisualize){
            visualizer.show();


        }
        else{
            visualizer.hide();
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.skip_next:
                direction = FORWARD;
                getIntentMethod(getNewPosition(direction));
                break;
            case R.id.play_pause:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    isPlaying = false;
                    playpauseBtn.setImageResource(R.drawable.ic_play);
                    albumArt.animate().scaleX(1f).scaleY(1f).setDuration(300);
                }
                else{
                    mediaPlayer.start();
                    isPlaying = true;
                    playpauseBtn.setImageResource(R.drawable.ic_pause);
                    albumArt.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300);
                }
                break;
            case R.id.skip_prev:
                direction = BACK;
                getIntentMethod(getNewPosition(direction));
                break;
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.shuffle:
                if(isShuffle){
                    isShuffle = false;
                    position = musicFiles.indexOf(listSongs.get(position));
                    listSongs = (ArrayList<MusicFiles>) musicFiles.clone();
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle);
                }else{
                    isShuffle = true;
                    MusicFiles song = listSongs.get(position);
                    listSongs.remove(song);
                    Collections.shuffle(listSongs);
                    listSongs.add(position, song);
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
                break;
            case R.id.repeat:
                if(isRepeat){
                    isRepeat = false;
                    repeatBtn.setImageResource(R.drawable.ic_repeat);
                }else{
                    isRepeat = true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on);
                }
                break;
            case R.id.player_menu:
                PowerMenu powerMenu = new PowerMenu.Builder(this)
                        .addItem(new PowerMenuItem("Enable visualizer", true))
                        .addItem(new PowerMenuItem("Disable visualizer", true))
                        .addItem(new PowerMenuItem("Equalizer", true))
                        .setLifecycleOwner(this)
                        .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                        .setMenuRadius(30f)
                        .setMenuShadow(10f)
                        .setSelectedEffect(true)
                        .setOnMenuItemClickListener(onMenuItemClickListener)
                        .build();
                powerMenu.showAsDropDown(view);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }
    private OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
            if(position == 0){
                isVisualize = true;
                visualizer.show();

            }
            else if(position == 1){
                isVisualize = false;
                visualizer.hide();
            }
            else{
                fragment.show(getSupportFragmentManager(), "eq");
            }
        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float xp = motionEvent.getX();;
        float seekBarPosition = (xp)/  waveformSeekBar.getWidth() * waveformSeekBar.getMaxProgress();
        seekBarPosition = seekBarPosition < 0 ?0:seekBarPosition;
        seekBarPosition= Math.min(seekBarPosition, waveformSeekBar.getMaxProgress());

        switch (view.getId()){
            case R.id.skip_next:
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        shouldClick = true;
                        nextBtn.animate().scaleX(0.8f).scaleY(0.8f).setDuration(50);
                        break;
                    case MotionEvent.ACTION_UP:
                        nextBtn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50);
                        if(shouldClick)
                            view.performClick();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        prevBtn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50);
                        shouldClick = false;
                        break;

                }
                return true;
            case R.id.skip_prev:
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        shouldClick = true;
                        prevBtn.animate().scaleX(0.8f).scaleY(0.8f).setDuration(50);
                        break;
                    case MotionEvent.ACTION_UP:
                        prevBtn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50);
                        if(shouldClick)
                            view.performClick();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        prevBtn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50);
                        shouldClick = false;
                        break;

                }
                return true;
            case R.id.waveformSeekBar:
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if(mediaPlayer != null){
                            waveformSeekBar.setProgress(seekBarPosition);
                            mediaPlayer.seekTo((int) (waveformSeekBar.getProgress() * 1000));
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        waveformSeekBar.setProgress(seekBarPosition);
                        break;
                }

        }
        return true;
    }
}