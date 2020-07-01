package com.kylecombes.micrepeater;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

public class SettingActivity extends Activity {

    ImageButton exitButton;
    Switch firebaseSwitch;
    RadioGroup radioGroup;
    RadioButton checkedRadioButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        //sets the setting window dimensions to fill 80% of screen
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.6));

        firebaseSwitch = findViewById(R.id.firebase_switch);
//        firebaseSwitch.setChecked(MainActivity.firebaseAnalyticsOn);
        exitButton = findViewById(R.id.setting_exit);
        radioGroup = findViewById(R.id.output_options);
//        setCheckedButton(MainActivity.streamType);

        firebaseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                MainActivity.activateFirebase(isChecked);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.alarm_option:
//                        MainActivity.streamType = AudioManager.STREAM_ALARM;
                        break;
                    case R.id.music_option:
//                        MainActivity.streamType = AudioManager.STREAM_MUSIC;
                        break;
                    case R.id.voice_call_option:
                    default:
//                        MainActivity.streamType = AudioManager.STREAM_VOICE_CALL;
                }
            }
        });
    }
/*
    private void setCheckedButton(int streamType) {
        switch (streamType) {
            case AudioManager.STREAM_ALARM:
                checkedRadioButton = findViewById(R.id.alarm_option);
                break;
            case AudioManager.STREAM_MUSIC:
                checkedRadioButton = findViewById(R.id.music_option);
                break;
            case AudioManager.STREAM_VOICE_CALL:
            default:
                checkedRadioButton = findViewById(R.id.voice_call_option);
        }
        checkedRadioButton.setChecked(true);
    }

    public void exitPage(View view) {
        finish();
    }*/
}
