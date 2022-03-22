package com.polyap.music_player;

import static android.content.ContentValues.TAG;

import static com.polyap.music_player.AlbumDetailsAdapter.albumFiles;
import static com.polyap.music_player.PlayerActivity.FORWARD;
import static com.polyap.music_player.PlayerActivity.isChangedMusic;
import static com.polyap.music_player.SongFragment.recyclerViewSong;
import static com.polyap.music_player.SongFragment.sortDirection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
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
    public static boolean isShuffle = false, isRepeat = false, isVisualize = true;
    public static MusicFiles oldMusicPlayed;
    public static MusicFiles currentMusicPlaying;
    public static String MY_SORT_PREF = "SortOrder";
    public static String sortOrderText;
    String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    static ArrayList<MusicFiles> musicFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();

    }

    private void Init(){
        ViewPager2 viewPager2 = findViewById(R.id.main_view_pager2);
        TabLayout tabLayout = findViewById(R.id.main_tab_layout);
        ViewPager2Adapter viewPager2Adapter = new ViewPager2Adapter(this);
        viewPager2Adapter.addFragments(new SongFragment(), "Songs");
        viewPager2Adapter.addFragments(new AlbumFragment(), "Albums");
        viewPager2.setAdapter(viewPager2Adapter);
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
        String direction = preferences.getString("direction", FORWARD);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        /*MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_option).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));*/
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        String userInput = s.toLowerCase(Locale.ROOT);
        ArrayList<MusicFiles> files = new ArrayList<>();
        for(MusicFiles song: musicFiles){
            if(song.getTitle().toLowerCase(Locale.ROOT).contains(userInput)){
                files.add(song);
            }
        }
        SongFragment.musicAdapter.updateList(files);
        return true;
    }


}