package com.polyap.music_player;

import static com.polyap.music_player.MainActivity.currentMusicPlaying;
import static com.polyap.music_player.MainActivity.musicFiles;
import static com.polyap.music_player.MainActivity.oldMusicPlayed;
import static com.polyap.music_player.PlayerActivity.isChangedMusic;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {

    static RecyclerView recyclerViewAlbum;
    AlbumAdapter albumAdapter;
    static ArrayList<MusicFiles> albums = new ArrayList<>();
    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        recyclerViewAlbum = view.findViewById(R.id.recycle_view_card);
        recyclerViewAlbum.setHasFixedSize(true);

        ArrayList<String> albumsName = new ArrayList<>();
        for(int i = 0; i < musicFiles.size(); i++){
            if(!albumsName.contains(musicFiles.get(i).getAlbum())){
                albums.add(musicFiles.get(i));//округление углов у карточек
                albumsName.add(musicFiles.get(i).getAlbum());
            }
        }
        albumAdapter = new AlbumAdapter(getContext(), albums);
        recyclerViewAlbum.setAdapter(albumAdapter);

        recyclerViewAlbum.setLayoutManager(new GridLayoutManager(getContext(), 2));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}