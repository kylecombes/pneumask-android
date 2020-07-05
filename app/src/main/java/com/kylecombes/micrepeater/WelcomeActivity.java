package com.kylecombes.micrepeater;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class WelcomeActivity extends FragmentActivity {

    private static final int[] LAYOUT_FILES = {
            R.layout.welcome1, R.layout.welcome2, R.layout.welcome3, R.layout.welcome4, R.layout.welcome5, R.layout.welcome6,
    };
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this, LAYOUT_FILES);
        viewPager.setAdapter(pagerAdapter);

        final Button backButton = findViewById(R.id.welcome_wizard_back_button);
        final Button nextButton = findViewById(R.id.welcome_wizard_next_button);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            public void onPageSelected(int position) {
                backButton.setVisibility(viewPager.getCurrentItem() == 0 ? View.INVISIBLE : View.VISIBLE);
                nextButton.setText(viewPager.getCurrentItem() == getLastItemIndex() ? R.string.finish : R.string.next);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true); //getItem(-1) for previous
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPager.getCurrentItem() == getLastItemIndex()) {
                    // If on the last page, then pressing the Finish button will finish this activity and start the main activity
                    // Also set welcomeWizardCompleted to false so the welcome wizard doesn't run again on the same device
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                            .putBoolean("welcomeWizardCompleted", true).apply();
                    finish();
                }
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }
        });
    }

    private int getLastItemIndex() {
        return LAYOUT_FILES.length - 1;
    }

}
