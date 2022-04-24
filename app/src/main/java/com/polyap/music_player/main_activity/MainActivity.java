package com.polyap.music_player.main_activity;


import static com.polyap.music_player.player_activity.PlayerActivity.BACK;
import static com.polyap.music_player.player_activity.PlayerActivity.MUSIC_LIST;
import static com.polyap.music_player.player_activity.PlayerActivity.QUEUE_MUSIC;
import static com.polyap.music_player.album_fragment.AlbumFragment.albums;
import static com.polyap.music_player.music_service.MusicService.MUSIC_FILE;
import static com.polyap.music_player.music_service.MusicService.MUSIC_LAST_PLAYED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;


import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.polyap.music_player.R;
import com.polyap.music_player.album_fragment.AlbumFragment;
import com.polyap.music_player.music_service.MusicService;
import com.polyap.music_player.object_serializer.ObjectSerializer;
import com.polyap.music_player.song_fragment.MusicFiles;
import com.polyap.music_player.song_fragment.SongFragment;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Главная активити, которая появляется после загрузочного экрана
 */
public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public final static int MIN_MUSIC_DURATION = 30;
    public final static int REQUEST_CODE = 1;
    public static boolean ALL_PERMISSIONS_GRANTED = false;
    public static boolean isShuffle = false, isRepeat = false, isVisualize = true, isEqualize = true;
    public static MusicFiles oldMusicPlayed;
    public static MusicFiles currentMusicPlaying;
    public static String MY_SORT_PREF = "SortOrder";
    public static String sortOrderText;
    public static boolean SHOW_MINI_PLAYER = false;
    public static ArrayList<MusicFiles> lastMusicQueue = null;
    public static int lastMusicPosition = -1;
    public static MusicService musicServiceMain;
    ViewPager2 viewPager2;
    SearchView searchView;
    static boolean isInit = false;


    String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.FOREGROUND_SERVICE
    };

    public static ArrayList<MusicFiles> musicFiles;

    /**
     * метод AppCompatActivity, вызывается при создании активити
     *
     * @param savedInstanceState сохранённое состояние
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();
    }

    /**
     * инициализация элементов активити
     */
    private void Init() {
        viewPager2 = findViewById(R.id.main_view_pager2);
        TabLayout tabLayout = findViewById(R.id.main_tab_layout);
        ViewPager2Adapter viewPager2Adapter = new ViewPager2Adapter(this);
        viewPager2Adapter.addFragments(new SongFragment(), "Songs");
        viewPager2Adapter.addFragments(new AlbumFragment(), "Albums");
        viewPager2.setAdapter(viewPager2Adapter);
        viewPager2.setPageTransformer(new Pager2_ZoomOutTransformer());


        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                tab.setText(viewPager2Adapter.getPageTitle(position));
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayoutMediator.attach();


    }

    /**
     * Проверка разрешений
     *
     * @param context     контекст
     * @param permissions разрешения, необхоидмые для работы приложения
     * @return True - если все разрешения получены, иначе - false
     */
    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Функция, которая запускает проверку наличия разрешений и выполняет дальнейшую инициализацию приложения
     */
    private void permission() {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
        } else {
            ALL_PERMISSIONS_GRANTED = true;
            musicFiles = getAllAudio();
            if (!isInit) {
                restoreMusic();
            }
            isInit = true;
            Init();
        }

    }

    /**
     * Проверка результатов, полученных от пользователя при запросах разрешений
     *
     * @param requestCode  код запроса
     * @param permissions  разрешения
     * @param grantResults полученные результаты
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean isAllGranted = true;
                for (int i = 0; i < PERMISSIONS.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false;
                        break;
                    }
                }
                ALL_PERMISSIONS_GRANTED = isAllGranted;
            }
            if (ALL_PERMISSIONS_GRANTED) {
                musicFiles = getAllAudio();
                if (!isInit) {
                    restoreMusic();
                }
                isInit = true;
                Init();

            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
            }

        }
    }

    /**
     * Поиск доступных треков на устройстве
     *
     * @return список треков
     */
    public ArrayList<MusicFiles> getAllAudio() {
        SharedPreferences preferences = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE);
        String sortOrder = preferences.getString("sorting", "sortByName");
        String direction = preferences.getString("direction", BACK);
        ArrayList<MusicFiles> tmpAudioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String order = null;

        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED
        };

        Cursor cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String albumId = cursor.getString(5);
                String id = cursor.getString(6);
                String size = cursor.getString(7);
                String date = cursor.getString(8);

                MusicFiles musicFiles = new MusicFiles(title, album, artist, duration, path, albumId, id, date, size);

                if (duration != null && Integer.parseInt(duration) / 1000 > MIN_MUSIC_DURATION && new File(musicFiles.getPath()).exists()) {
                    tmpAudioList.add(musicFiles);
                }
            }
        }
        switch (sortOrder) {
            case "sortByName":
                sortOrderText = "Sort by name";
                SongFragment.sortDirection = direction;
                Collections.sort(tmpAudioList, new SongFragment.EventDetailSortByName());
                break;
            case "sortByDate":
                sortOrderText = "Sort by date";
                SongFragment.sortDirection = direction;
                Collections.sort(tmpAudioList, new SongFragment.EventDetailSortByDate());
                break;
            case "sortBySize":
                sortOrderText = "Sort by size";
                SongFragment.sortDirection = direction;
                Collections.sort(tmpAudioList, new SongFragment.EventDetailSortBySize());
                break;
        }

        return tmpAudioList;
    }


    /**
     * Создание панели поиска треков
     *
     * @param menu меню
     * @return результат конструктора супер класса
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * @param s строка
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    /**
     * Поиск треков по введённому тексту
     *
     * @param s строка
     * @return true
     */
    @Override
    public boolean onQueryTextChange(String s) {
        String userInput = s.toLowerCase(Locale.ROOT);
        ArrayList<MusicFiles> files = new ArrayList<>();
        int position = viewPager2.getCurrentItem();
        ArrayList<MusicFiles> album = new ArrayList<>();
        if (AlbumFragment.albumAdapter != null) {
            album = albums;
        }
        if (position == 0) {
            for (MusicFiles song : musicFiles) {
                if (song.getTitle().toLowerCase(Locale.ROOT).contains(userInput) || song.getArtist().toLowerCase(Locale.ROOT).contains(userInput)) {
                    files.add(song);
                }
            }
            SongFragment.musicAdapter.updateList(files);
            if (AlbumFragment.albumAdapter != null) {
                AlbumFragment.albumAdapter.updateList(album);
            }

        } else {
            for (MusicFiles song : albums) {
                if (song.getAlbum().toLowerCase(Locale.ROOT).contains(userInput) || song.getArtist().toLowerCase(Locale.ROOT).contains(userInput)) {
                    files.add(song);
                }
            }
            AlbumFragment.albumAdapter.updateList(files);
            SongFragment.musicAdapter.updateList(musicFiles);
        }

        return true;
    }

    /**
     * восстанавливаем последний запущенный трек
     */
    public void restoreMusic() {
        SharedPreferences preferences = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
        SharedPreferences preferencesQueue = getSharedPreferences(QUEUE_MUSIC, MODE_PRIVATE);
        try {
            lastMusicPosition = preferences.getInt(MUSIC_FILE, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            lastMusicQueue = (ArrayList<MusicFiles>) ObjectSerializer.deserialize(preferencesQueue.getString(MUSIC_LIST, ObjectSerializer.serialize(new ArrayList<MusicFiles>())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (lastMusicPosition != -1 && lastMusicQueue != null && lastMusicQueue.size() != 0) {
            for (int i = 0; i < lastMusicQueue.size(); i++) {
                if (!new File(lastMusicQueue.get(i).getPath()).exists()) {
                    lastMusicQueue.remove(i);
                    lastMusicPosition = lastMusicPosition == i ? 0 : lastMusicPosition;
                }
            }
            if (lastMusicQueue.size() == 0) {
                SHOW_MINI_PLAYER = false;
                if (musicFiles != null && musicFiles.size() != 0) {
                    lastMusicPosition = 0;
                    lastMusicQueue = (ArrayList<MusicFiles>) musicFiles.clone();
                    SHOW_MINI_PLAYER = true;
                }
            } else
                SHOW_MINI_PLAYER = true;
        } else {

            SHOW_MINI_PLAYER = false;
            if (musicFiles != null && musicFiles.size() != 0) {
                lastMusicPosition = 0;
                lastMusicQueue = (ArrayList<MusicFiles>) musicFiles.clone();
                SHOW_MINI_PLAYER = true;
            }
        }
    }


}

/**
 * трансформер для ViewPager(анимация перелистывания между фрагментами)
 */
class Pager2_ZoomOutTransformer implements ViewPager2.PageTransformer {

    private static final float MIN_SCALE = 0.65f;
    private static final float MIN_ALPHA = 0.3f;

    /**
     * Алгоритм трансформации при перелистывании
     *
     * @param page     страница
     * @param position позиция
     */
    @Override
    public void transformPage(@NonNull View page, float position) {
        if (position < -1) {
            page.setAlpha(0);
        } else if (position <= 1) {
            page.setScaleX(Math.max(MIN_SCALE, 1 - Math.abs(position)));
            page.setScaleY(Math.max(MIN_SCALE, 1 - Math.abs(position)));
            page.setAlpha(Math.max(MIN_ALPHA, 1 - Math.abs(position)));
        } else {
            page.setAlpha(0);
        }
    }
}