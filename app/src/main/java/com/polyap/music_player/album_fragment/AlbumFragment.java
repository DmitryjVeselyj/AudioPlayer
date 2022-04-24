package com.polyap.music_player.album_fragment;

import static com.polyap.music_player.main_activity.MainActivity.musicFiles;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.polyap.music_player.R;
import com.polyap.music_player.song_fragment.MusicFiles;

import java.util.ArrayList;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

/**
 * Фрагмент вкладки "альбомы"
 */
public class AlbumFragment extends Fragment {

    public static RecyclerView recyclerViewAlbum;
    public static AlbumAdapter albumAdapter;
    public static ArrayList<MusicFiles> albums = new ArrayList<>();

    public AlbumFragment() {
        // Required empty public constructor
    }

    /**
     * метод Fragment, вызывается при создании фрагмента
     *
     * @param savedInstanceState сохранённое состояние
     */
    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Инициализация
     *
     * @param inflater           экземляр класса, создающего из layout-файла View-элемент
     * @param container          ViewGroup контейнер
     * @param savedInstanceState сохранённое состояние
     * @return View
     */
    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        recyclerViewAlbum = view.findViewById(R.id.recycle_view_card);
        recyclerViewAlbum.setHasFixedSize(true);
        new FastScrollerBuilder(recyclerViewAlbum).useMd2Style().build();
        ArrayList<String> albumsName = new ArrayList<>();
        for (int i = 0; i < musicFiles.size(); i++) {
            if (!albumsName.contains(musicFiles.get(i).getAlbum())) {
                albums.add(musicFiles.get(i));//округление углов у карточек
                albumsName.add(musicFiles.get(i).getAlbum());
            }
        }
        albumAdapter = new AlbumAdapter(getContext(), albums);
        recyclerViewAlbum.setAdapter(albumAdapter);

        recyclerViewAlbum.setLayoutManager(new GridLayoutManager(getContext(), 2));
        return view;
    }

}