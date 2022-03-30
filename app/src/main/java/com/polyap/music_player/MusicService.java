package com.polyap.music_player;

import static com.polyap.music_player.ApplicationClass.ACTION_DISMISS;
import static com.polyap.music_player.ApplicationClass.ACTION_NEXT;
import static com.polyap.music_player.ApplicationClass.ACTION_PLAY;
import static com.polyap.music_player.ApplicationClass.ACTION_PREVIOUS;
import static com.polyap.music_player.ApplicationClass.CHANNEL_ID_2;
import static com.polyap.music_player.MainActivity.isRepeat;
import static com.polyap.music_player.MainActivity.lastMusicPosition;
import static com.polyap.music_player.MainActivity.lastMusicQueue;
import static com.polyap.music_player.MainActivity.musicServiceMain;
import static com.polyap.music_player.NowPlayingFragmentBottom.plPosition;
import static com.polyap.music_player.PlayerActivity.FORWARD;
import static com.polyap.music_player.PlayerActivity.MUSIC_LIST;
import static com.polyap.music_player.PlayerActivity.QUEUE_MUSIC;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer ;
    Uri uri;
    int position = plPosition;
    ActionPlaying actionPlaying;
    UpdateBottomPlayer updateBottomPlayer;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    MediaSessionCompat mediaSessionCompat;
    @Override
    public void onCreate() {
        super.onCreate();

        position = lastMusicPosition;
        plPosition = lastMusicPosition;
        mediaSessionCompat= new MediaSessionCompat(getBaseContext(), "My Audio");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(12312);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");
        if(actionName != null){
            switch (actionName){
                case "playPause":
                    if(actionPlaying != null){
                        actionPlaying.btn_play_pauseClicked();
                    }
                    else if(updateBottomPlayer != null){
                        updateBottomPlayer.btn_play_pauseClicked();
                        updateBottomPlayer.updatePlayer();
                    }
                    break;
                case "next":
                    if(actionPlaying != null){
                        actionPlaying.btn_nextClicked();
                    }
                     else if(updateBottomPlayer != null){
                        updateBottomPlayer.btn_nextClicked();
                        updateBottomPlayer.updatePlayer();
                    }

                    break;
                case "previous":
                    if(actionPlaying != null){
                        actionPlaying.btn_prevClicked();
                    }
                    else if(updateBottomPlayer != null){
                        updateBottomPlayer.btn_prevClicked();
                        updateBottomPlayer.updatePlayer();
                    }
                    break;
                case "dismiss":
                    if(actionPlaying != null){
                        actionPlaying.btn_dismiss();
                    }
                    else if(updateBottomPlayer != null){
                        updateBottomPlayer.btn_dismiss();
                    }
                    break;
            }
        }
        return START_STICKY;
    }
    public void playMedia(int position){

        this.position = position;
        plPosition = position;
        uri = Uri.parse(lastMusicQueue.get(position).getPath());
        if(mediaPlayer != null){//это свой код 12 53
            mediaPlayer.stop();
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(String.valueOf(uri));
                mediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
        }
        mediaPlayer.start();
    }
    void start(){
        mediaPlayer.start();
    }
    boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    void stop(){
        mediaPlayer.stop();
    }
    void release() {
        mediaPlayer.release();
    }
    int getDuration(){
        return mediaPlayer.getDuration();
    }
    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    void createMediaPlayer(int position){
        uri = Uri.parse(lastMusicQueue.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    void pause(){
        mediaPlayer.pause();
    }
    void reset() {
        mediaPlayer.reset();
    }
    void prepare() throws IOException {
        mediaPlayer.prepare();
    }
    void setDataSource(Uri uri) throws IOException {
        mediaPlayer.setDataSource(getBaseContext(), uri);
    }
    int getAudioSessionId(){
        return mediaPlayer.getAudioSessionId();
    }

    void setPosition(int position){
        this.position = position;
        plPosition = position;
    }
    void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(actionPlaying != null){
            actionPlaying.btn_nextClicked();
        }
        else if(updateBottomPlayer != null){
            updateBottomPlayer.btn_nextClicked();
            updateBottomPlayer.updatePlayer();
        }
    }

    private int getNewPosition(String direction){
        if(direction.equals(FORWARD)){
            return (position + 1) % lastMusicQueue.size();
        }
        else{
            return (position - 1) < 0 ? (lastMusicQueue.size()) - 1: position - 1;
        }
    }
    void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }
    void setCallBack1(UpdateBottomPlayer updateBottomPlayer){
        this.updateBottomPlayer = updateBottomPlayer;
    }


    void showNotification(int playPauseBtn, float playbackSpeed, int state){
        Intent intent = new Intent(this , PlayerActivity.class);
        PendingIntent contentIntent= PendingIntent.getActivity(this, 0, intent, 0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0,prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_DISMISS);
        PendingIntent dismissPending = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        final Intent notificationIntent = new Intent(this, SplashActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(lastMusicQueue.get(position).getAlbumId()));

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.msc_back1);
            //e.printStackTrace();
        }

        Notification notification = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(bitmap)
                .setContentIntent(notifyPendingIntent)
                .setContentTitle(lastMusicQueue.get(position).getTitle())
                .setContentText(lastMusicQueue.get(position).getArtist())
                .addAction(R.drawable.ic_previous_2, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_next_2, "Next", nextPending)
                .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build();
        PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder().build();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration()).build());
            mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(state, mediaPlayer.getCurrentPosition(), playbackSpeed,  SystemClock.elapsedRealtime())
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build());

        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(12312, notification);
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                mediaPlayer.seekTo((int) pos);
                mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), playbackSpeed,  SystemClock.elapsedRealtime())
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build());

            }
        });


    }

    public void saveTracks(){//для норм кликов
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
    void saveLastTrack(int position){
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE).edit();
        editor.putInt(MUSIC_FILE, position);
        editor.apply();
    }


}
