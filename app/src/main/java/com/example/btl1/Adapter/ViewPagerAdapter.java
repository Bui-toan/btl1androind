package com.example.btl1.Adapter;



import android.content.ContentResolver;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.btl1.Fragments.AllSongFragment;
import com.example.btl1.Fragments.FavSongFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final ContentResolver contentResolver;
    private final String[] title = {"Bài Hát", "Ưa Thích"};

    public ViewPagerAdapter(FragmentManager fm, ContentResolver contentResolver) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.contentResolver = contentResolver;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return AllSongFragment.getInstance(position, contentResolver);
            case 1:
                return FavSongFragment.getInstance(position);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}

