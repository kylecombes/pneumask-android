package com.kylecombes.micrepeater;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class WelcomeActivity extends FragmentActivity {
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager2 viewPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private FragmentStateAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        final Button backbutton = (Button) findViewById(R.id.back_button);
        final Button nextbutton = (Button) findViewById(R.id.next_button);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true); //getItem(-1) for previous
                if (viewPager.getCurrentItem() == 0) {
                    //if the user returns to the first page, then the back button turns invisible
                    backbutton.setVisibility(View.INVISIBLE);
                } else if(viewPager.getCurrentItem() == 4) {
                    //if the user goes to the last page, but then returns to the pages before, the "next" button should read next instead of finish
                    nextbutton.setText(R.string.next);
                }
            }
        });

        nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewPager.getCurrentItem() == 5){
                    //if on the last page, then hitting the "next" button will finish welcome activity and start main activity
                    finish();
                }
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                if (viewPager.getCurrentItem() == 5){
                    //if on the last page, the "next" button will read "finish" instead of "next"
                    nextbutton.setText(R.string.finish);
                } else if (viewPager.getCurrentItem() != 0) {
                    //if not on the first page, then the back button will be visible, otherwise the back button is invisible
                    backbutton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

}
