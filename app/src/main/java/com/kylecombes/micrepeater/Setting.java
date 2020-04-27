package com.kylecombes.micrepeater;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

public class Setting extends MainActivity {

    ImageButton exitButton;
    Switch firebaseSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        //sets the setting window dimensions to fill 80% of screen
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*0.8), (int)(height * 0.6));

        firebaseSwitch = findViewById(R.id.firebase_switch);
        exitButton = findViewById(R.id.setting_exit);

        firebaseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.activateFirebase();
            }
        });
    }

    public void exitPage(View view) {
        finish();
    }
}
