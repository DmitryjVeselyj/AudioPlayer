package com.polyap.music_player;

import static android.content.ContentValues.TAG;

import static com.polyap.music_player.AlbumAdapter.albumFilesFragment;
import static com.polyap.music_player.AlbumDetailsAdapter.albumFiles;
import static com.polyap.music_player.AlbumFragment.albums;
import static com.polyap.music_player.MusicAdapter.musicFilesList;
import static com.polyap.music_player.MusicService.MUSIC_FILE;
import static com.polyap.music_player.MusicService.MUSIC_LAST_PLAYED;
import static com.polyap.music_player.PlayerActivity.BACK;
import static com.polyap.music_player.PlayerActivity.FORWARD;
import static com.polyap.music_player.PlayerActivity.MUSIC_LIST;
import static com.polyap.music_player.PlayerActivity.QUEUE_MUSIC;
import static com.polyap.music_player.PlayerActivity.isChangedMusic;
import static com.polyap.music_player.SongFragment.recyclerViewSong;
import static com.polyap.music_player.SongFragment.sortDirection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.Toast;


import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
    public static String PATH_TO_FRAG = null;
    public static ArrayList<MusicFiles> lastMusicQueue = null;
    public static int lastMusicPosition=-1;
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

    static ArrayList<MusicFiles> musicFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        permission();

    }

    private void Init(){
        viewPager2 = findViewById(R.id.main_view_pager2);
        TabLayout tabLayout = findViewById(R.id.main_tab_layout);
        ViewPager2Adapter viewPager2Adapter = new ViewPager2Adapter(this);
        viewPager2Adapter.addFragments(new SongFragment(), "Songs");
        viewPager2Adapter.addFragments(new AlbumFragment(), "Albums");
        viewPager2.setAdapter(viewPager2Adapter);
        viewPager2.setPageTransformer(new Pager2_ZoomOutTransformer());


        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy(){
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
    private void permission() {
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
        }
        else{
            ALL_PERMISSIONS_GRANTED = true;
            musicFiles = getAllAudio();
            if(!isInit) {
                restoreMusic();
            }
            isInit = true;
            Init();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if(grantResults.length > 0) {
                boolean isAllGranted = true;
                for (int i = 0; i < PERMISSIONS.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false;
                        break;
                    }
                }
                ALL_PERMISSIONS_GRANTED = isAllGranted;
            }
            if(ALL_PERMISSIONS_GRANTED){
                musicFiles = getAllAudio();
                if(!isInit) {
                    restoreMusic();
                }
                isInit = true;
                Init();

            }
            else{
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
            }

        }
    }

    public ArrayList<MusicFiles> getAllAudio(){
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

        Cursor cursor = getApplicationContext().getContentResolver().query(uri, projection,null,null,null);
        if(cursor != null){
            while(cursor.moveToNext()){
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String albumId = cursor.getString(5);
                String id = cursor.getString(6);
                String size = cursor.getString(7);
                String date = cursor.getString(8);

                MusicFiles musicFiles = new MusicFiles(title,album,artist,duration, path, albumId, id, date, size);

                if(duration != null && Integer.parseInt(duration)/ 1000 > MIN_MUSIC_DURATION && new File(musicFiles.getPath()).exists()) {
                    tmpAudioList.add(musicFiles);
                }
            }
        }
        switch (sortOrder){
            case "sortByName":
                sortOrderText = "Sort by name" ;
                sortDirection = direction;
                Collections.sort(tmpAudioList, new SongFragment.EventDetailSortByName() );
                break;
            case "sortByDate":
                sortOrderText = "Sort by date" ;
                sortDirection = direction;
                Collections.sort(tmpAudioList, new SongFragment.EventDetailSortByDate());
                break;
            case "sortBySize":
                sortOrderText = "Sort by size" ;
                sortDirection = direction;
                Collections.sort(tmpAudioList, new SongFragment.EventDetailSortBySize());
                break;
        }

        return tmpAudioList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        String userInput = s.toLowerCase(Locale.ROOT);
        ArrayList<MusicFiles> files = new ArrayList<>();
        int position = viewPager2.getCurrentItem();
        ArrayList<MusicFiles> album = new ArrayList<>();
        if(AlbumFragment.albumAdapter != null){
            album = albums;
        }
        if(position == 0) {
            for (MusicFiles song : musicFiles) {
                if (song.getTitle().toLowerCase(Locale.ROOT).contains(userInput) || song.getArtist().toLowerCase(Locale.ROOT).contains(userInput)) {
                    files.add(song);
                }
            }
            SongFragment.musicAdapter.updateList(files);
            if(AlbumFragment.albumAdapter != null){
                AlbumFragment.albumAdapter.updateList(album);
            }

        }
        else{
            for (MusicFiles song : albums) {
                if (song.getAlbum().toLowerCase(Locale.ROOT).contains(userInput)|| song.getArtist().toLowerCase(Locale.ROOT).contains(userInput)) {
                    files.add(song);
                }
            }
            AlbumFragment.albumAdapter.updateList(files);
            SongFragment.musicAdapter.updateList(musicFiles);
        }

        return true;
    }
    public void restoreMusic(){
        SharedPreferences preferences = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
        SharedPreferences preferencesQueue = getSharedPreferences(QUEUE_MUSIC, MODE_PRIVATE);
        try {
            lastMusicPosition = preferences.getInt(MUSIC_FILE, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            lastMusicQueue = (ArrayList<MusicFiles>) ObjectSerializer.deserialize(preferencesQueue.getString(MUSIC_LIST, ObjectSerializer.serialize(new ArrayList<MusicFiles>())));
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(lastMusicPosition != -1 && lastMusicQueue != null && lastMusicQueue.size() != 0){
            for(int i = 0; i < lastMusicQueue.size(); i++){
                if(!new File(lastMusicQueue.get(i).getPath()).exists()){
                    lastMusicQueue.remove(i);
                    lastMusicPosition = lastMusicPosition==i?0:lastMusicPosition;
                }
            }
            if(lastMusicQueue.size() == 0){
                SHOW_MINI_PLAYER = false;
                if(musicFiles != null && musicFiles.size() != 0) {
                    lastMusicPosition = 0;
                    lastMusicQueue = (ArrayList<MusicFiles>) musicFiles.clone();
                    SHOW_MINI_PLAYER=true;
                }
            }
            else
                SHOW_MINI_PLAYER = true;
        }
        else{

            SHOW_MINI_PLAYER = false;
            if(musicFiles != null && musicFiles.size() != 0) {
                lastMusicPosition = 0;
                lastMusicQueue = (ArrayList<MusicFiles>) musicFiles.clone();
                SHOW_MINI_PLAYER=true;
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}

class Pager2_ZoomOutTransformer implements ViewPager2.PageTransformer {

    private static final float MIN_SCALE = 0.65f;
    private static final float MIN_ALPHA = 0.3f;

    @Override
    public void transformPage(@NonNull View page, float position) {
        if (position <-1){  // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0);
        }
        else if (position <=1){ // [-1,1]
            page.setScaleX(Math.max(MIN_SCALE,1-Math.abs(position)));
            page.setScaleY(Math.max(MIN_SCALE,1-Math.abs(position)));
            page.setAlpha(Math.max(MIN_ALPHA,1-Math.abs(position)));
        }
        else {  // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0);
        }
    }
}