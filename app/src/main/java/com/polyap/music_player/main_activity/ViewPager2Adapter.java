package com.polyap.music_player.main_activity;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.polyap.music_player.album_fragment.AlbumFragment;
import com.polyap.music_player.song_fragment.SongFragment;

import java.util.ArrayList;

/**
 * адаптер для ViewPager2
 */
public class ViewPager2Adapter extends FragmentStateAdapter {

    private ArrayList<Fragment> fragments;
    private ArrayList<String> titles;

    /**
     * конструктор
     * @param fragmentActivity фрагмент-активити
     */
    public ViewPager2Adapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.fragments = new ArrayList<>();
        this.titles = new ArrayList<>();
    }

    /**
     * Метод, добавляющий фрагменты
     * @param fragment фрагмент
     * @param title заголовок
     */
    void addFragments(Fragment fragment, String title){
        fragments.add(fragment);
        titles.add(title);
    }

    /**
     * Создание фрагментов
     * @param position позиция
     * @return фрагмент
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0) {
            return new SongFragment();
        }
        else {
            return new AlbumFragment();
        }
    }

    /**
     * @return количество фрагментов
     */
    @Override
    public int getItemCount() {
        return fragments.size();
    }

    /**
     * @param position позиция
     * @return заголовок
     */
    public CharSequence getPageTitle(int position){
        return titles.get(position);
    }
}
