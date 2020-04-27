package com.kylecombes.micrepeater;

import android.media.Image;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class Information extends MainActivity {

    ImageButton exitButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        //sets the setting window dimensions to fill portion of screen
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*0.8), (int)(height * 0.6));

        //allow for links
        TextView text = (TextView) findViewById(R.id.Info_text);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        exitButton = findViewById(R.id.info_exit);
    }

    //exits out of activity and returns to main activity
    public void exitPage(View view) {
        finish();
    }
}
