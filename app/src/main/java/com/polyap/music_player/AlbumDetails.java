package com.polyap.music_player;

import static com.polyap.music_player.AlbumDetailsAdapter.albumFiles;
import static com.polyap.music_player.MainActivity.currentMusicPlaying;
import static com.polyap.music_player.MainActivity.lastMusicPosition;
import static com.polyap.music_player.MainActivity.musicFiles;
import static com.polyap.music_player.MainActivity.musicServiceMain;
import static com.polyap.music_player.MainActivity.oldMusicPlayed;;
import static com.polyap.music_player.MusicAdapter.musicFilesList;
import static com.polyap.music_player.PlayerActivity.getPosition;
import static com.polyap.music_player.PlayerActivity.isChangedMusic;
import static com.polyap.music_player.PlayerActivity.setWindowFlag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {
    private final Uri ALBUMART_URI = Uri.parse("content://media/external/audio/albumart");
    static RecyclerView recyclerView;
    ImageView albumPhoto;
    ImageView albumPhotoBottom;
    TextView albumNameText;
    TextView artistNameText;
    Toolbar textTop;
    String albumName;
    String albumArtist;
    AlbumDetailsAdapter albumDetailsAdapter;
    int position = 0;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();


    private TypedValue mTypedValue = new TypedValue();
    private int mHeaderHeight;
    private int mMinHeaderTranslation;
    private int mActionBarHeight;
    private RectF mRect1 = new RectF();
    private RectF mRect2 = new RectF();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        hideSystemBars();
        init();


    }
    private void init(){
        recyclerView = findViewById(R.id.recycle_view_details);
        albumPhoto = findViewById(R.id.album_photo);
        albumNameText = findViewById(R.id.album_name);
        artistNameText = findViewById(R.id.artist_name);
        albumPhotoBottom = findViewById(R.id.album_photo_bottom);
        //textTop = findViewById(R.id.text_top);
        albumName = getIntent().getStringExtra("albumName");
        albumArtist = getIntent().getStringExtra("artistName");
        position = getIntent().getIntExtra("position", -1);

        int j = 0;
        for(int i = 0; i < musicFiles.size(); i++){
            if(albumName.equals(musicFiles.get(i).getAlbum())){
                albumSongs.add(j , musicFiles.get(i));
                j++;
            }
        }
        if(!(albumSongs.size() < 1)){
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
        albumNameText.setText(albumName);
        artistNameText.setText(albumArtist);
        //textTop.setTitle(albumName);
        //textTop.setTitleTextColor(Color.WHITE);
        loadImage(position);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //int scrollY = getScrollY();
                //sticky actionbar
                //textTop.setTranslationY(Math.max(-scrollY, mMinHeaderTranslation));
                //header_logo --> actionbar icon
                //float ratio = clamp(textTop.getTranslationY(), 0.0f, 1.0f);
                //actionbar title alpha
                //textTop.setAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
                //---------------------------------
                //better way thanks to @cyrilmottier
                //textTop.animate().alpha(1f);
            }
        });


    }

    private void hideSystemBars() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
        statusBartoTransparent();
    }
    private void loadImage(int position){
        Uri imageUri = Uri.withAppendedPath(ALBUMART_URI, String.valueOf(musicFiles.get(position).getAlbumId()));
        if(imageUri != null){
            Glide.with(this).load(imageUri).error(R.drawable.msc_back1).into(albumPhoto);
            Glide.with(this).load(imageUri).into(albumPhotoBottom);
        }
    }

    @Override
    protected void onResume() {
        if(AlbumDetails.recyclerView != null && isChangedMusic){
            RecyclerView.Adapter albumDetailsAdapter = AlbumDetails.recyclerView.getAdapter();
            if(albumDetailsAdapter != null){
                if(oldMusicPlayed != null)
                    albumDetailsAdapter.notifyItemChanged(getPosition((ArrayList<MusicFiles>) albumSongs, oldMusicPlayed));
                if(currentMusicPlaying != null)
                    albumDetailsAdapter.notifyItemChanged(getPosition((ArrayList<MusicFiles>) albumSongs, currentMusicPlaying));
            }
        }
        super.onResume();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}