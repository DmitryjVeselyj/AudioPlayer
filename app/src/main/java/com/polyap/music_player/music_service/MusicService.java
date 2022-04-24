package com.polyap.music_player.music_service;

import static com.polyap.music_player.music_service.ApplicationClass.ACTION_DISMISS;
import static com.polyap.music_player.music_service.ApplicationClass.ACTION_NEXT;
import static com.polyap.music_player.music_service.ApplicationClass.ACTION_PLAY;
import static com.polyap.music_player.music_service.ApplicationClass.ACTION_PREVIOUS;
import static com.polyap.music_player.music_service.ApplicationClass.CHANNEL_ID_2;
import static com.polyap.music_player.main_activity.MainActivity.lastMusicPosition;
import static com.polyap.music_player.main_activity.MainActivity.lastMusicQueue;
import static com.polyap.music_player.player_activity.PlayerActivity.FORWARD;
import static com.polyap.music_player.player_activity.PlayerActivity.MUSIC_LIST;
import static com.polyap.music_player.player_activity.PlayerActivity.QUEUE_MUSIC;
import static com.polyap.music_player.bottom_player.NowPlayingFragmentBottom.plPosition;

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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.polyap.music_player.player_activity.PlayerActivity;
import com.polyap.music_player.R;
import com.polyap.music_player.main_activity.SplashActivity;
import com.polyap.music_player.interfaces.ActionPlaying;
import com.polyap.music_player.interfaces.UpdateBottomPlayer;
import com.polyap.music_player.object_serializer.ObjectSerializer;

import java.io.IOException;

/**
 * Сервис собственной персоной
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder mBinder = new MyBinder();
    public static MediaPlayer mediaPlayer ;
    Uri uri;
    int position = plPosition;
    public ActionPlaying actionPlaying;
    public UpdateBottomPlayer updateBottomPlayer;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    MediaSessionCompat mediaSessionCompat;

    /**
     * Метод, вызываемый при создании сервиса
     */
    @Override
    public void onCreate() {
        super.onCreate();

        position = lastMusicPosition;
        plPosition = lastMusicPosition;
        mediaSessionCompat= new MediaSessionCompat(getBaseContext(), "My Audio");
    }

    /**
     * Если убрана задача, убираем сервис из строки уведомлений
     * @param rootIntent Intent объект
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        deleteNot();
        super.onTaskRemoved(rootIntent);
    }

    /**
     * Удаление из строки уведомлений
     */
    public void deleteNot(){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(12312);
    }


    /**
     * MyBinder класс
     */
    public class MyBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * Обработка действий сервиса
     * @param intent Intent объект
     * @param flags флаги
     * @param startId int
     * @return int
     */
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

    /**
     * Запускаем плеер
     */
    public void start(){
        mediaPlayer.start();
    }

    /**
     * @return Предоставляется читателю самому догадаться, что возращает этот метод
     */
    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    /**
     * останавливаем плеер
     */
    public void stop(){
        mediaPlayer.stop();
    }

    /**
     * @return длина трека
     */
    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    /**
     * Перемещение плеера на нужную позицию
     * @param position позиция в треке
     */
    public void seekTo(int position){
        mediaPlayer.seekTo(position);
    }

    /**
     * Получение позиции плеера
     * @return текущее положение плеера
     */
    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * Создание плеера по нужному треку
     * @param position позиция
     */
    public void createMediaPlayer(int position){
        uri = Uri.parse(lastMusicQueue.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    /**
     * Приостанавливаем плеер
     */
    public void pause(){
        mediaPlayer.pause();
    }

    /**
     * Сброс плеера
     */
    public void reset() {
        mediaPlayer.reset();
    }

    /**
     * Как неудивительно, но это подготовка плеера
     * @throws IOException
     */
    public void prepare() throws IOException {
        mediaPlayer.prepare();
    }

    /**
     * Устанавливаем нужный трек в качестве источника
     * @param uri Я называю его Юрий
     * @throws IOException
     */
    public void setDataSource(Uri uri) throws IOException {
        mediaPlayer.setDataSource(getBaseContext(), uri);
    }

    /**
     * Получение id сессии
     * @return идентификатор сессии
     */
    public int getAudioSessionId(){
        return mediaPlayer.getAudioSessionId();
    }

    /**
     * Устанавливаем позицию
     * @param position позиция
     */
    public void setPosition(int position){
        this.position = position;
        plPosition = position;
    }

    /**
     * устанавливаем прослушивателя для медиаплеера
     */
    public void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    /**
     * Прослушиватель окончания воспроизведения треков
     * @param mediaPlayer это именно то, что вы думаете
     */
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


    /**
     * Колбэк для строки уведомлений
     * @param actionPlaying интерфейс
     */
    public void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }

    /**
     * Функция с довольно загадочным названием
     * Колбэк для мини-плеера
     * @param updateBottomPlayer интерфейс
     */
    public void setCallBack1(UpdateBottomPlayer updateBottomPlayer){
        this.updateBottomPlayer = updateBottomPlayer;
    }


    /**
     * Отображение сервиса в строке уведомлений
     * @param playPauseBtn иконка
     * @param playbackSpeed скорость обновления SeekBar-а в строке уведомлений
     * @param state состояние
     */
    public void showNotification(int playPauseBtn, float playbackSpeed, int state){
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


    /**
     * Сохранение последнего трека
     * @param position позиция
     */
    public void saveLastTrack(int position){
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE).edit();
        editor.putInt(MUSIC_FILE, position);
        editor.apply();
    }


}
