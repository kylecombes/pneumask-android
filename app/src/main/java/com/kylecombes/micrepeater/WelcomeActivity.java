package com.kylecombes.micrepeater;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class WelcomeActivity extends FragmentActivity {

    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        final Button backButton = findViewById(R.id.back_button);
        final Button nextButton = findViewById(R.id.next_button);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                if (viewPager.getCurrentItem() == 0) {
                    // If the user returns to the first page, then the back button turns invisible
                    backButton.setVisibility(View.INVISIBLE);
                } else if (viewPager.getCurrentItem() == 4) {
                    // If the user goes to the last page, but then returns to the pages before, the "next" button should read next instead of finish
                    nextButton.setText(R.string.next);
                } else if (viewPager.getCurrentItem() == 5) {
                    // If on the last page, the "next" button will read "finish" instead of "next"
                    nextButton.setText(R.string.finish);
                } else if (viewPager.getCurrentItem() == 1) {
                    // If not on the first page, then the back button will be visible, otherwise the back button is invisible
                    backButton.setVisibility(View.VISIBLE);
                }
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
                if(viewPager.getCurrentItem() == 5){
                    //if on the last page, then hitting the "next" button will finish welcome activity and start main activity
                    finish();
                }
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }
        });
    }

}
