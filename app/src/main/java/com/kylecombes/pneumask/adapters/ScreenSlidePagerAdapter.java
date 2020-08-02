package com.kylecombes.pneumask.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.kylecombes.pneumask.fragments.PrivacySettingsFragment;
import com.kylecombes.pneumask.fragments.WelcomeWizardInfoPageFragment;

import com.kylecombes.pneumask.R;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {
    private int[] mLayoutFiles;

    public ScreenSlidePagerAdapter(FragmentActivity fa, int[] layoutFiles) {
        super(fa);
        mLayoutFiles = layoutFiles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (mLayoutFiles[position] == R.layout.fragment_privacy_settings) {
            return PrivacySettingsFragment.newInstance();
        }
        return WelcomeWizardInfoPageFragment.newInstance(position, mLayoutFiles[position]);
    }

    @Override
    public int getItemCount() {
        return mLayoutFiles.length;
    }
}