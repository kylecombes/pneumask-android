package com.kylecombes.micrepeater;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.kylecombes.micrepeater.ui.main.AppStateViewModel;
import com.kylecombes.micrepeater.ui.main.SectionsPagerAdapter;

public class MainActivity2 extends AppCompatActivity {

    private AppStateViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        mViewModel = new ViewModelProvider(this).get(AppStateViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBluetoothSCOListener();
    }


    public void registerBluetoothSCOListener() {
        // Register a listener to respond to Bluetooth connect/disconnect events
        IntentFilter intent = new IntentFilter();
        intent.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intent.addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED");
        intent.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
        registerReceiver(BluetoothStateReceiver.getInstance(), intent);

        // Register a callback to get notified about Bluetooth connect/disconnect events
        BluetoothStateReceiver.getInstance().registerStateChangeReceiver(
                new BluetoothStateReceiver.StateChangeReceiver() {
                    public void stateChanged(boolean deviceConnected, boolean scoAudioConnected,
                                             int batteryPercentage) {
                        mViewModel.setMicBatteryPercentage(batteryPercentage);
                    }
                }
        );
    }
}