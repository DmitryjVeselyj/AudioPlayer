package com.polyap.music_player;

import static com.polyap.music_player.MainActivity.musicFiles;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;


public class SongFragment extends Fragment {

    MusicAdapter musicAdapter;
    RecyclerView recyclerView;
    public SongFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song, container, false);
        recyclerView = view.findViewById(R.id.music_recycle_list);
        recyclerView.setHasFixedSize(true);
        musicAdapter = new MusicAdapter(getContext(), musicFiles);
        recyclerView.setAdapter(musicAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager((getContext()), RecyclerView.VERTICAL, false));
        return view;
    }
}