package com.example.btl1.Adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.btl1.Fragments.AllSongFragment;
import com.example.btl1.Fragments.FavSongFragment;
import com.example.btl1.R;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final String[] title = {"Bài Hát", "Ưa Thích"};

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AllSongFragment();
            case 1:
                return FavSongFragment.newInstance(R.drawable.allsongalbum);
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
