package com.polyap.music_player;

import static android.graphics.Color.green;
import static com.polyap.music_player.AlbumDetailsAdapter.albumFiles;
import static com.polyap.music_player.MainActivity.currentMusicPlaying;
import static com.polyap.music_player.MainActivity.isEqualize;
import static com.polyap.music_player.MainActivity.isRepeat;
import static com.polyap.music_player.MainActivity.isShuffle;
import static com.polyap.music_player.MainActivity.isVisualize;
import static com.polyap.music_player.MainActivity.lastMusicPosition;
import static com.polyap.music_player.MainActivity.lastMusicQueue;
import static com.polyap.music_player.MainActivity.musicServiceMain;
import static com.polyap.music_player.MainActivity.oldMusicPlayed;
import static com.polyap.music_player.MusicAdapter.musicFilesList;

import static com.polyap.music_player.NowPlayingFragmentBottom.updateCurrentSong;
import static com.polyap.po_equalizer.DialogEqualizerFragment.mEqualizer;
;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentManager;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.base.BaseVisualizer;
import com.gauravk.audiovisualizer.visualizer.WaveVisualizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.masoudss.lib.WaveformSeekBar;
import com.polyap.po_equalizer.DialogEqualizerFragment;
import com.polyap.po_equalizer.Settings;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class PlayerActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, ServiceConnection, MediaPlayer.OnCompletionListener, ActionPlaying {
    TextView songName, artistName, durationPlayed, durationTotal;
    ImageView albumArt, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn, menuBtn;
    public static ImageView equalizerBtn;
    public static ImageView visualizerBtn;
    RelativeLayout playerLayout, layoutTop, layoutCard;
    FloatingActionButton playpauseBtn;
    WaveVisualizer visualizer;
    SeekBar seekBar;
    String sender;
    public static boolean isPlaying = false;
    public static boolean isChangedMusic = true;
    static boolean init = false;
    public static int position = -1;
    static Uri uri;
    //static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private int oldAudioSessionId = -2;
    final static String FORWARD = "forward";
    final static String BACK = "back";
    final static String NOTHING ="nothing";
    public static String direction = FORWARD;
    WaveformSeekBar waveformSeekBar;
    ColorDrawable lastColor = new ColorDrawable(Color.BLACK);
    boolean shouldClick = false;
    static DialogEqualizerFragment fragment;
    static ArrayList<MusicFiles> tmp;
    static MusicService musicService = musicServiceMain;
    public static String QUEUE_MUSIC = "QueueMusic";
    public static String MUSIC_LIST = "MUSIC_LIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
        statusBartoTransparent();

        InitViews();
        checkingSender();
        initService();
        init = true;
        mainListeners();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                saveTracks();

            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }
    void initService(){
        musicService = musicServiceMain;
        if(musicService == null) {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("servicePosition", position);
            startService(intent);
            isPlaying = true;
        }
        else{
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(12312);

            if(musicService.mediaPlayer != null && sender != null && sender.equals("BottomPlayer")) {

                designActivity();
            }
            else{
               // isPlaying = true;
                getIntentMethod(position);
            }
        }
    }
    public void statusBartoTransparent(){
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
    void addPaddingTop(RelativeLayout layout){

        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);

        }
        layout.setPadding(0, result, 0, 0);
        resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if(resourceId > 0){
            result = getResources().getDimensionPixelSize(resourceId);
        }


    }
    @Override
    public void onBackPressed() {
        visualizer.release();
        super.onBackPressed();
        overridePendingTransition(R.anim.close_top_to_bottom, R.anim.close_bottom_to_top);
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
    private void saveTracks(){
        SharedPreferences preferences = getSharedPreferences(QUEUE_MUSIC, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        try {
            editor.putString(MUSIC_LIST, ObjectSerializer.serialize(lastMusicQueue));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        editor.commit();
    }
    private void loadTracks(){
        SharedPreferences preferences = getSharedPreferences(QUEUE_MUSIC, Context.MODE_PRIVATE);
        try {
            lastMusicQueue = (ArrayList<MusicFiles>) ObjectSerializer.deserialize(preferences.getString(MUSIC_LIST, ObjectSerializer.serialize(new ArrayList<MusicFiles>())));
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        if (visualizer != null)
            visualizer.setEnabled(false);
            visualizer.release();
        musicService.setCallBack(null);

        super.onDestroy();
    }
    public static int getPosition(ArrayList<MusicFiles> tracks, MusicFiles currentFile){
        for(int i = 0 ; i < tracks.size(); i++){
            if(tracks.get(i).getId().equals(currentFile.getId())){
                return i;
            }
        }
        return -1;
    }

    public static void updateSongList(){
            if (SongFragment.recyclerViewSong != null) {//nfstr

                RecyclerView.Adapter songAdapter = SongFragment.recyclerViewSong.getAdapter();
                if (songAdapter != null) {
                    if(oldMusicPlayed != null) {
                        songAdapter.notifyItemChanged(getPosition((ArrayList<MusicFiles>) musicFilesList, oldMusicPlayed));
                    }
                    if(currentMusicPlaying != null) {
                        songAdapter.notifyItemChanged(getPosition((ArrayList<MusicFiles>) musicFilesList, currentMusicPlaying));
                    }
                }
            }

            if(AlbumDetails.recyclerView != null){
                RecyclerView.Adapter albumDetailsAdapter = AlbumDetails.recyclerView.getAdapter();
                if(albumDetailsAdapter != null){
                    if(oldMusicPlayed != null)
                        albumDetailsAdapter.notifyItemChanged(getPosition((ArrayList<MusicFiles>) albumFiles, oldMusicPlayed));
                    if(currentMusicPlaying != null)
                        albumDetailsAdapter.notifyItemChanged(getPosition((ArrayList<MusicFiles>) albumFiles, currentMusicPlaying));
                }
            }
            isChangedMusic = false;
    }

    private int getNewPosition(String direction){
        if(isRepeat)
            return position;
        if(direction.equals(FORWARD)){
            return (position + 1) % lastMusicQueue.size();
        }
        else{
            return (position - 1) < 0 ? (lastMusicQueue.size()) - 1: position - 1;
        }
    }

    private void mainListeners(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                durationPlayed.setText(formattedTime((int) seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(seekBar.getProgress());
                musicService.seekTo((int) (seekBar.getProgress() * 1000));
                if(musicService.isPlaying()) {
                    musicService.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PLAYING);
                }
                else{
                    musicService.showNotification(R.drawable.ic_play_n, 0f, PlaybackStateCompat.STATE_PLAYING);
                }
            }
        });


       PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService != null){
                    int currentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                    durationPlayed.setText(formattedTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });


        equalizerBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                fragment.show(getSupportFragmentManager(), "eq");
                isEqualize = true;
                return false;
            }
        });


    }
    void justShuffle(){
        MusicFiles song = lastMusicQueue.get(position);
        lastMusicQueue.remove(song);
        Collections.shuffle(lastMusicQueue);
        lastMusicQueue.add(position, song);
        lastMusicPosition = position;
    }
    private void checkingSender(){
        sender = getIntent().getStringExtra("sender");
        if(sender != null && sender.equals("AlbumDetails")){
            lastMusicQueue = (ArrayList<MusicFiles>) albumFiles.clone();
            tmp = albumFiles;
            this.position = getIntent().getIntExtra("position", -1);
            if(isShuffle)
                justShuffle();
        }
        else if(sender != null && sender.equals("MainActivity")){
            lastMusicQueue = (ArrayList<MusicFiles>) ((ArrayList<MusicFiles>)musicFilesList).clone();
            tmp = (ArrayList<MusicFiles>) musicFilesList;
            this.position = getIntent().getIntExtra("position", -1);
            if(isShuffle)
                justShuffle();

        }
        else if(sender != null && sender.equals("BottomPlayer")){
            //tmp = (ArrayList<MusicFiles>) lastMusicQueue; используем lstMusic
            this.position= lastMusicPosition;
        }
    }
    private void getIntentMethod(int position) {
        this.position = position;
        musicService.setPosition(position);
        if(lastMusicQueue != null){
            uri = Uri.parse(lastMusicQueue.get(position).getPath());
            loadImages(position);
        }
            if (musicService.mediaPlayer != null) {
                musicService.stop();
                musicService.reset();
                try {
                    musicService.setDataSource(uri);
                    musicService.prepare();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                musicService.createMediaPlayer(position);
                musicService.OnCompleted();
                //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            }
            if (isPlaying ) {
                playpauseBtn.setImageResource(R.drawable.ic_pause);
                albumArt.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300);

                musicService.start();
                musicService.showNotification(R.drawable.ic_pause_n, 1f,  PlaybackStateCompat.STATE_PLAYING);
            } else {
                playpauseBtn.setImageResource(R.drawable.ic_play);
                musicService.showNotification(R.drawable.ic_play_n, 0f,  PlaybackStateCompat.STATE_PLAYING);
            }

            seekBar.setProgress(0);
            seekBar.setMax(musicService.getDuration() / 1000);


            int audioSessionId = musicService.getAudioSessionId();
            if (audioSessionId != -1 && oldAudioSessionId != audioSessionId) {
                visualizer.setAudioSessionId(audioSessionId);
                oldAudioSessionId = audioSessionId;


            }
            oldMusicPlayed = currentMusicPlaying;
            currentMusicPlaying = lastMusicQueue.get(position);
            if (currentMusicPlaying.equals(oldMusicPlayed) || oldMusicPlayed == null)
                isChangedMusic = true;
            else
                isChangedMusic = true;
            updateSongList();
            if (fragment != null) {
                fragment = DialogEqualizerFragment.newBuilder()
                        .setAudioSessionId(audioSessionId)
                        .themeColor(ContextCompat.getColor(this, R.color.black))
                        .textColor(ContextCompat.getColor(this, R.color.white))
                        .accentAlpha(ContextCompat.getColor(this, R.color.purple_200))
                        .darkColor(ContextCompat.getColor(this, R.color.purple_200))
                        .setAccentColor(ContextCompat.getColor(this, R.color.purple_200))
                        .build();
            } else {
                fragment = DialogEqualizerFragment.newBuilder()
                        .setAudioSessionId(audioSessionId)
                        .themeColor(ContextCompat.getColor(this, R.color.black))
                        .textColor(ContextCompat.getColor(this, R.color.white))
                        .accentAlpha(ContextCompat.getColor(this, R.color.purple_200))
                        .darkColor(ContextCompat.getColor(this, R.color.purple_200))
                        .setAccentColor(ContextCompat.getColor(this, R.color.purple_200))
                        .build();
                equalizerUpdate();
            }

        //musicService.OnCompleted();
        //musicService.mediaPlayer.setOnCompletionListener(this);
        lastMusicPosition = position;
        musicService.saveLastTrack(position);

    }
    private void equalizerUpdate(){
        FragmentManager fm = getSupportFragmentManager();//дичайший костыль. Еле держится, но работает
        fm.beginTransaction().hide(fragment).commit();
        fragment.show(getSupportFragmentManager(), "eq");

        fm.beginTransaction().remove(fragment).commit();
        if(Settings.isEqualizerEnabled){
            equalizerBtn.setImageResource(R.drawable.ic_equalizer_on);
        }
        else{
            equalizerBtn.setImageResource(R.drawable.ic_equalizer);
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
        playerLayout = findViewById(R.id.player_activity);
        equalizerBtn = findViewById(R.id.equalizer_btn);
        visualizerBtn = findViewById(R.id.visualizer_btn);
        nextBtn.setOnTouchListener(this);
        nextBtn.setOnClickListener(this);

        playpauseBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        prevBtn.setOnTouchListener(this);
        backBtn.setOnClickListener(this);
        shuffleBtn.setOnClickListener(this);
        repeatBtn.setOnClickListener(this);
        menuBtn.setOnClickListener(this);
        equalizerBtn.setOnClickListener(this);
        visualizerBtn.setOnClickListener(this);

        playpauseBtn.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50);
        nextBtn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50);
        prevBtn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50);
        shuffleBtn.animate().scaleX(0.9f).scaleY(0.9f).setDuration(50);
        repeatBtn.animate().scaleX(0.9f).scaleY(0.9f).setDuration(50);
        equalizerBtn.animate().scaleX(0.9f).scaleY(0.9f).setDuration(50);

        seekBar = findViewById(R.id.seekBar_player);
        seekBar.setProgress(0);
        layoutTop= findViewById(R.id.layout_top_btn);//пока не нужно, но может пригодиться когда-то
        addPaddingTop(playerLayout);


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
                    hsl[2] = hsl[2] > (float) 0.90? (float)0.3:hsl[2];//поменять когда-то
                    int color = ColorUtils.HSLToColor(hsl);
                    visualizer.setColor(color);

                    seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                    ColorDrawable colorDrawable1 = new ColorDrawable(color);
                    ColorDrawable[] cd = {lastColor, colorDrawable1};
                    TransitionDrawable transitionDrawable = new TransitionDrawable(cd);
                    TransitionDrawable transitionDrawable1 = new TransitionDrawable(cd);
                    playerLayout.setBackground(transitionDrawable);

                    transitionDrawable.startTransition(400);
                    lastColor = colorDrawable1;

                }
            }
        });
    }
    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void loadImages(int position){

        final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(lastMusicQueue.get(position).getAlbumId()));
        Animation animationFirst = null;
        Animation animationSecond = null;
        if(direction.equals(FORWARD)) {
            animationFirst = AnimationUtils.loadAnimation(this, R.anim.slide_left);
            animationSecond = AnimationUtils.loadAnimation(this, R.anim.slide_left_sec);
        }
        else if(direction.equals(BACK)) {
            animationFirst = AnimationUtils.loadAnimation(this, R.anim.slide_right);
            animationSecond = AnimationUtils.loadAnimation(this, R.anim.slide_right_sec);
        }
        if(animationFirst != null) {
            Animation finalAnimationSecond = animationSecond;
            animationFirst.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    albumArt.startAnimation(finalAnimationSecond);
                    if(imageUri != null){
                        albumArt.setImageURI(imageUri);
                    }
                    if(albumArt.getDrawable() == null) {
                        albumArt.setImageResource(R.drawable.msc_back1);
                    }
                    changeVisualizerColor(visualizer);
                    songName.setText(lastMusicQueue.get(position).getTitle());
                    artistName.setText(lastMusicQueue.get(position).getArtist());
                    durationTotal.setText(formattedTime(Integer.parseInt(lastMusicQueue.get(position).getDuration()) / 1000));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            albumArt.startAnimation(animationFirst);
        }


        if(isRepeat){
            repeatBtn.setImageResource(R.drawable.ic_repeat7);
        }else{
            repeatBtn.setImageResource(R.drawable.ic_repeat7_of);
        }

        if(isShuffle){
            shuffleBtn.setImageResource(R.drawable.ic_shuffle10);;
        }else{
            shuffleBtn.setImageResource(R.drawable.ic_shuffle10_of);
        }

        if(isVisualize){
            visualizer.show();
            visualizerBtn.setImageResource(R.drawable.ic_visualizer_on);

        }
        else{
            visualizer.hide();
            visualizerBtn.setImageResource(R.drawable.ic_visualizer);
        }

        if(isEqualize){
            equalizerBtn.setImageResource(R.drawable.ic_equalizer_on);
        }
        else{
            equalizerBtn.setImageResource(R.drawable.ic_equalizer);
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
                if(musicService.isPlaying()){
                    musicService.pause();
                    isPlaying = false;
                    playpauseBtn.setImageResource(R.drawable.ic_play);
                    albumArt.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300);
                    musicService.showNotification(R.drawable.ic_play_n, 0f,  PlaybackStateCompat.STATE_PLAYING);
                }
                else{
                    musicService.start();
                    isPlaying = true;
                    playpauseBtn.setImageResource(R.drawable.ic_pause);
                    albumArt.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300);
                    musicService.showNotification(R.drawable.ic_pause_n, 1f,  PlaybackStateCompat.STATE_PLAYING);

                }
                updateCurrentSong();
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
                    position = tmp.indexOf(lastMusicQueue.get(position));
                    lastMusicQueue = (ArrayList<MusicFiles>) tmp.clone();
                    lastMusicPosition = position;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle10_of);
                }else{
                    isShuffle = true;
                    MusicFiles song = lastMusicQueue.get(position);
                    lastMusicQueue.remove(song);
                    Collections.shuffle(lastMusicQueue);
                    lastMusicQueue.add(position, song);
                    lastMusicPosition = position;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle10);
                }
                break;
            case R.id.repeat:
                if(isRepeat){
                    isRepeat = false;
                    repeatBtn.setImageResource(R.drawable.ic_repeat7_of);
                }else{
                    isRepeat = true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat7);
                }
                break;
            case R.id.player_menu:
                PowerMenu powerMenu = new PowerMenu.Builder(this)
                        .setMenuColorResource(R.color.black)
                        .setTextColorResource(R.color.white)
                        .addItem(new PowerMenuItem("Enable visualizer", false))
                        .addItem(new PowerMenuItem("Disable visualizer", false))
                        .addItem(new PowerMenuItem("Equalizer", false))
                        .setLifecycleOwner(this)
                        .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                        .setMenuRadius(30f)
                        .setMenuShadow(10f)
                        .setSelectedEffect(false)
                        .setOnMenuItemClickListener(onMenuItemClickListener)
                        .build();
                powerMenu.showAsDropDown(view);
                break;
            case R.id.equalizer_btn:
                if(Settings.isEqualizerEnabled){
                    Settings.isEqualizerEnabled = false;
                    equalizerBtn.setImageResource(R.drawable.ic_equalizer);
                    mEqualizer.setEnabled(false);
                    isEqualize = false;
                }
                else{
                    Settings.isEqualizerEnabled = true;
                    mEqualizer.setEnabled(true);
                    equalizerBtn.setImageResource(R.drawable.ic_equalizer_on);
                    isEqualize = true;
                }
                break;
            case R.id.visualizer_btn:
                if(isVisualize){
                    visualizer.hide();
                    isVisualize = false;
                    visualizerBtn.setImageResource(R.drawable.ic_visualizer);
                }
                else{
                    visualizer.show();
                    isVisualize = true;
                    visualizerBtn.setImageResource(R.drawable.ic_visualizer_on);
                }
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
                visualizerBtn.setImageResource(R.drawable.ic_visualizer_on);

            }
            else if(position == 1){
                isVisualize = false;
                visualizer.hide();
                visualizerBtn.setImageResource(R.drawable.ic_visualizer);
            }
            else{
                fragment.show(getSupportFragmentManager(), "eq");
                isEqualize =true;
            }
        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float xp = motionEvent.getX();;

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
        }
        return true;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) iBinder;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        //getIntentMethod(position);
    }
    private void designActivity(){
        musicService.setPosition(position);
        if(lastMusicQueue != null){
            uri = Uri.parse(lastMusicQueue.get(position).getPath());
            loadImages(position);
        }
        if (isPlaying) {
            playpauseBtn.setImageResource(R.drawable.ic_pause);
            albumArt.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300);
            musicService.showNotification(R.drawable.ic_pause_n, 1f,  PlaybackStateCompat.STATE_PLAYING);
        } else {
            playpauseBtn.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play_n, 0f, PlaybackStateCompat.STATE_PLAYING);
        }

        int audioSessionId = musicService.getAudioSessionId();
        if (audioSessionId != -1 && oldAudioSessionId != audioSessionId) {
            visualizer.setAudioSessionId(audioSessionId);
            oldAudioSessionId = audioSessionId;

        }
        seekBar.setProgress(0);
        seekBar.setMax(musicService.getDuration() / 1000);

    }
    @Override
    protected void onResume() {

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
       // unbindService(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(isRepeat) {
            direction = NOTHING;
            //musicService.playMedia(position);
            musicService.mediaPlayer.seekTo(0);
            musicService.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PAUSED);
            getIntentMethod(position);
        }
        else {
            direction = FORWARD;
            //musicService.playMedia(getNewPosition(direction));
            musicService.mediaPlayer.seekTo(0);
            musicService.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PAUSED);
            getIntentMethod(getNewPosition(direction));
        }
    }



    @Override
    public void btn_play_pauseClicked() {
        if(musicService.isPlaying()){
            musicService.pause();
            isPlaying = false;
            playpauseBtn.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play_n, 0f, PlaybackStateCompat.STATE_PAUSED);
            albumArt.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300);
        }
        else{
            musicService.start();
            isPlaying = true;
            playpauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PLAYING);
            albumArt.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300);
        }
        updateCurrentSong();
    }

    @Override
    public void btn_nextClicked() {
        direction = FORWARD;
        musicService.mediaPlayer.seekTo(0);
        if(isPlaying)
            musicService.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PAUSED);
        else
            musicService.showNotification(R.drawable.ic_play_n, 1f, PlaybackStateCompat.STATE_PAUSED);
        getIntentMethod(getNewPosition(FORWARD));
    }

    @Override
    public void btn_prevClicked() {
        direction = BACK;
        musicService.mediaPlayer.seekTo(0);
        if(isPlaying)
            musicService.showNotification(R.drawable.ic_pause_n, 1f, PlaybackStateCompat.STATE_PAUSED);
        else
            musicService.showNotification(R.drawable.ic_play_n, 1f, PlaybackStateCompat.STATE_PAUSED);
        getIntentMethod(getNewPosition(direction));
    }

    @Override
    public void btn_dismiss() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(12312);
        if(musicService != null){
            musicService.pause();
            isPlaying = false;
            playpauseBtn.setImageResource(R.drawable.ic_play);
        }
        updateCurrentSong();
    }
}