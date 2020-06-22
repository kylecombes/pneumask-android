package com.kylecombes.micrepeater;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.kylecombes.micrepeater.ScreenSlidePageFragment;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 6;
    public ScreenSlidePagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        return ScreenSlidePageFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}