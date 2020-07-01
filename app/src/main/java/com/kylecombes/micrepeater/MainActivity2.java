package com.kylecombes.micrepeater;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.kylecombes.micrepeater.ui.main.AppStateViewModel;
import com.kylecombes.micrepeater.ui.main.SectionsPagerAdapter;
import com.kylecombes.micrepeater.ui.main.VoiceAmplifierFragment;

import java.util.Objects;

public class MainActivity2 extends AppCompatActivity implements VoiceAmplificationController {

    private static final String TAG = AppCompatActivity.class.getCanonicalName();

    private AudioManager audioManager;
    Intent audioRelayServiceIntent;
    private AppStateViewModel mViewModel;
    private VoiceAmplifierFragment mVoiceAmpFragment;
    boolean mScoAudioConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        //using a fake to run welcome wizard every time right now (for developing purposes). For app to function properly, replace fake with isFirstRun
        Boolean fake = true;
        if (fake) {
            //show start activity
            startActivity(new Intent(MainActivity2.this, WelcomeActivity.class));
            //set isFirstRun to false so the welcome wizard doesn't run again on the same device
            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                    .putBoolean("isFirstRun", false).apply();
        }
        setContentView(R.layout.activity_main2);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        mViewModel = new ViewModelProvider(this).get(AppStateViewModel.class);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        requestNeededPermissions();
        registerBluetoothSCOListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        activateBluetoothSco();
    }

    /**
     * Checks to make sure the app has the needed permissions:
     * - RECORD_AUDIO
     * If those permissions have not been granted, they will be requested.
     */
    private void requestNeededPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1234);
        }
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
                        if (deviceConnected && !scoAudioConnected) {
                            activateBluetoothSco();
                        }
                        mScoAudioConnected = scoAudioConnected;
                        mViewModel.setMicBatteryPercentage(batteryPercentage);
                        Boolean micIsOn = Objects.requireNonNull(mViewModel.getMicIsOn().getValue());
                        if (micIsOn && !scoAudioConnected) {
                            stopAmplification();
                        }
                    }
                }
        );
    }

    /**
     * Starts the AudioRelayService.
     */
    public void startAmplification() {
        Log.d(TAG, "Starting amplification...");
        audioRelayServiceIntent = new Intent(this, AudioRelayService.class);
        Integer streamType = Objects.requireNonNull(mViewModel.getStreamType().getValue());
        audioRelayServiceIntent.putExtra(AudioRelayService.STREAM_KEY, streamType);
        // Set default volume control
        setVolumeControlStream(streamType);
        startService(audioRelayServiceIntent);
        mViewModel.setMicIsOn(true);
    }

    /**
     * Stops the AudioRelayService.
     */
    public void stopAmplification() {
        Log.d(TAG, "Stopping amplification...");
        AudioRelayService ars = AudioRelayService.getInstance();
        if (ars != null) {
            ars.shutDown();
        }
        mViewModel.setMicIsOn(false);
    }

    private void activateBluetoothSco() {
        if (audioManager == null || !audioManager.isBluetoothScoAvailableOffCall()) {
            Log.e(TAG, "SCO ist not available. Recording is not possible");
            mScoAudioConnected = false;
            return;
        }

        if (!audioManager.isBluetoothScoOn()) {
            audioManager.startBluetoothSco();
        }
    }

}