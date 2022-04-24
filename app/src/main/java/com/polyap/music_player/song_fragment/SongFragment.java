package com.polyap.music_player.song_fragment;

import static android.content.Context.MODE_PRIVATE;
import static com.polyap.music_player.main_activity.MainActivity.MY_SORT_PREF;
import static com.polyap.music_player.main_activity.MainActivity.currentMusicPlaying;
import static com.polyap.music_player.main_activity.MainActivity.isShuffle;
import static com.polyap.music_player.main_activity.MainActivity.musicFiles;
import static com.polyap.music_player.main_activity.MainActivity.oldMusicPlayed;
import static com.polyap.music_player.main_activity.MainActivity.sortOrderText;
import static com.polyap.music_player.song_fragment.MusicAdapter.musicFilesList;
import static com.polyap.music_player.player_activity.PlayerActivity.BACK;
import static com.polyap.music_player.player_activity.PlayerActivity.FORWARD;
import static com.polyap.music_player.player_activity.PlayerActivity.getPosition;
import static com.polyap.music_player.player_activity.PlayerActivity.isChangedMusic;
import static com.polyap.music_player.player_activity.PlayerActivity.isPlaying;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.polyap.music_player.player_activity.PlayerActivity;
import com.polyap.music_player.R;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;


/**
 * Фрагмент вкладки "треки"
 */
public class SongFragment extends Fragment {

    public static MusicAdapter musicAdapter;
    public static RecyclerView recyclerViewSong;
    public static String sortDirection = FORWARD;
    ImageView sortBtn;
    ImageView shuffleBtn;
    TextView sortText;

    public SongFragment() {
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
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song, container, false);
        recyclerViewSong = view.findViewById(R.id.music_recycle_list);
        recyclerViewSong.setHasFixedSize(true);
        musicAdapter = new MusicAdapter(getContext(), musicFiles);
        recyclerViewSong.setAdapter(musicAdapter);
        recyclerViewSong.setLayoutManager(new LinearLayoutManager((getContext()), RecyclerView.VERTICAL, false));
        new FastScrollerBuilder(recyclerViewSong).useMd2Style().build();

        shuffleBtn = view.findViewById(R.id.shuffleSong);
        sortBtn = view.findViewById(R.id.sortBtn);
        sortText = view.findViewById(R.id.sortText);
        if (sortDirection.equals(FORWARD)) {
            Collections.reverse(musicFilesList);
            musicAdapter.notifyDataSetChanged();
            sortBtn.setImageResource(R.drawable.ic_arrow_up);
        } else {
            sortBtn.setImageResource(R.drawable.ic_arrow_down);
        }
        sortText.setText(sortOrderText);
        sortText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
                    @Override
                    public void onItemClick(int itemPosition, PowerMenuItem item) {
                        saveSettingsSort(itemPosition);
                    }
                };
                PowerMenu recycleMenu = new PowerMenu.Builder(getContext())
                        .setMenuColorResource(R.color.black)
                        .setTextColorResource(R.color.white)
                        .addItem(new PowerMenuItem("By name", false))
                        .addItem(new PowerMenuItem("By date", false))
                        .addItem(new PowerMenuItem("By size", false))
                        .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                        .setMenuRadius(30f)
                        .setMenuShadow(10f)
                        .setSelectedEffect(false)
                        .setOnMenuItemClickListener(onMenuItemClickListener)
                        .setAutoDismiss(true)
                        .build();

                recycleMenu.showAsAnchorLeftTop(view);
            }
        });
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sortDirection.equals(FORWARD)) {
                    sortDirection = BACK;
                    Collections.reverse(musicFilesList);
                    musicAdapter.notifyDataSetChanged();
                    sortBtn.setImageResource(R.drawable.ic_arrow_down);
                } else {
                    sortDirection = FORWARD;
                    Collections.reverse(musicFilesList);
                    musicAdapter.notifyDataSetChanged();
                    sortBtn.setImageResource(R.drawable.ic_arrow_up);
                }
                SharedPreferences.Editor editor = getContext().getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();
                editor.putString("direction", sortDirection);
                editor.apply();
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PlayerActivity.class);
                intent.putExtra("sender", "MainActivity");
                isShuffle = true;
                Random random = new Random();
                int pos = random.nextInt(musicFilesList.size());
                intent.putExtra("position", pos);
                isPlaying = true;
                Activity activity = (Activity) getContext();
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.bottom_to_top, R.anim.top_to_bottom);
                //getContext().startActivity(intent);
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        if (SongFragment.recyclerViewSong != null && isChangedMusic) {

            RecyclerView.Adapter songAdapter = SongFragment.recyclerViewSong.getAdapter();
            if (songAdapter != null) {
                if (oldMusicPlayed != null)
                    songAdapter.notifyItemChanged(getPosition((ArrayList<MusicFiles>) musicFilesList, oldMusicPlayed));
                if (currentMusicPlaying != null)
                    songAdapter.notifyItemChanged(getPosition((ArrayList<MusicFiles>) musicFilesList, currentMusicPlaying));
            }
        }
        //isChangedMusic = false;
        super.onResume();
    }

    public void saveSettingsSort(int position) {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();
        switch (position) {
            case 0:
                editor.putString("sorting", "sortByName");
                editor.putString("direction", sortDirection);
                editor.apply();
                Collections.sort(musicFilesList, new EventDetailSortByName());
                sortOrderText = "Sort by name";
                ;
                musicAdapter.notifyDataSetChanged();
                sortText.setText(sortOrderText);
                //getActivity().recreate();
                break;
            case 1:
                editor.putString("sorting", "sortByDate");
                editor.putString("direction", sortDirection);
                editor.apply();
                Collections.sort(musicFilesList, new SongFragment.EventDetailSortByDate());
                musicAdapter.notifyDataSetChanged();
                sortOrderText = "Sort by date";
                sortText.setText(sortOrderText);
                //getActivity().recreate();
                break;
            case 2:
                editor.putString("sorting", "sortBySize");
                editor.putString("direction", sortDirection);
                editor.apply();
                Collections.sort(musicFilesList, new SongFragment.EventDetailSortBySize());
                musicAdapter.notifyDataSetChanged();
                sortOrderText = "Sort by size";
                sortText.setText(sortOrderText);
                //getActivity().recreate();
                break;
        }
    }

    public static class EventDetailSortByName implements java.util.Comparator<MusicFiles> {
        @Override
        public int compare(MusicFiles customerEvents1, MusicFiles customerEvents2) {
            String name1, name2;
            name1 = customerEvents1.getTitle().toLowerCase().trim();
            name2 = customerEvents2.getTitle().toLowerCase().trim();
            return name2.compareTo(name1);
        }
    }

    public static class EventDetailSortByDate implements java.util.Comparator<MusicFiles> {
        @Override
        public int compare(MusicFiles customerEvents1, MusicFiles customerEvents2) {
            int name1date, name2date;
            name1date = Integer.parseInt(customerEvents1.getDateAdded().toLowerCase().trim());
            name2date = Integer.parseInt(customerEvents2.getDateAdded().toLowerCase().trim());
            return name1date - name2date;
        }
    }

    public static class EventDetailSortBySize implements java.util.Comparator<MusicFiles> {
        @Override
        public int compare(MusicFiles customerEvents1, MusicFiles customerEvents2) {
            int name1size, name2size;
            name1size = Integer.parseInt(customerEvents1.getSize().toLowerCase().trim());
            name2size = Integer.parseInt(customerEvents2.getSize().toLowerCase().trim());
            return name1size - name2size;
        }
    }

}