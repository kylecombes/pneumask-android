package com.kylecombes.micrepeater;

import android.Manifest;
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

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements VoiceAmplificationController {

    private static final String TAG = AppCompatActivity.class.getCanonicalName();

    private AudioManager audioManager;
    Intent audioRelayServiceIntent;
    private AppStateViewModel mViewModel;
    boolean mBluetoothDeviceConnected = false;
    boolean mScoAudioConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean welcomeWizardCompleted = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("welcomeWizardCompleted", false);
        if (!welcomeWizardCompleted) {
            // Show Welcome activity
            startActivity(new Intent(this, WelcomeActivity.class));
        }
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        mViewModel = new ViewModelProvider(this).get(AppStateViewModel.class);
        AudioRelayService relayService = AudioRelayService.getInstance();
        if (relayService != null && relayService.isRelayingActive()) {
            mBluetoothDeviceConnected = true;
            mScoAudioConnected = true;
            mViewModel.setAppMode(AppStateViewModel.AppMode.AMPLIFYING_ON);
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        requestNeededPermissions();
        registerBluetoothSCOListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        activateBluetoothScoIfNecessary();
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
        intent.addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED");
        Intent i = registerReceiver(BluetoothStateReceiver.getInstance(audioManager), intent);
        if (i != null) {
            int currentScoAudioState = i.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            mScoAudioConnected = currentScoAudioState == AudioManager.SCO_AUDIO_STATE_CONNECTED;
        }

        // Register a callback to get notified about Bluetooth connect/disconnect events
        BluetoothStateReceiver.getInstance(audioManager).registerStateChangeReceiver(
                new BluetoothStateReceiver.StateChangeReceiver() {
                    @Override
                    public void bluetoothDeviceConnectionStateChange(boolean isConnected) {
                        // If we just lost the connection to the device, forget the battery level
                        if (mBluetoothDeviceConnected && !isConnected) {
                            mViewModel.setMicBatteryPercentage(null);
                            if (mViewModel.getAppMode().getValue() == AppStateViewModel.AppMode.AMPLIFYING_ON) {
                                stopAmplification();
                            }
                        }
                        mBluetoothDeviceConnected = isConnected;
                        updateAppMode();
                        activateBluetoothScoIfNecessary();
                    }

                    @Override
                    public void scoAudioConnectionStateChange(boolean isConnected) {
                        mScoAudioConnected = isConnected;
                        AppStateViewModel.AppMode mode = mViewModel.getAppMode().getValue();
                        if (!mScoAudioConnected && mode == AppStateViewModel.AppMode.AMPLIFYING_ON) {
                            stopAmplification();
                        }
                        activateBluetoothScoIfNecessary();
                        updateAppMode();
                    }

                    @Override
                    public void batteryLevelChanged(Integer batteryLevel) {
                        mViewModel.setMicBatteryPercentage(batteryLevel);
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
        String streamType = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getString("audioOutput", "voice");
        if(streamType.equals("voice")){
            audioRelayServiceIntent.putExtra(AudioRelayService.STREAM_KEY, AudioManager.STREAM_VOICE_CALL);
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        } else if(streamType.equals("alarm")){
            audioRelayServiceIntent.putExtra(AudioRelayService.STREAM_KEY, AudioManager.STREAM_ALARM);
            setVolumeControlStream(AudioManager.STREAM_ALARM);
        } else {
            audioRelayServiceIntent.putExtra(AudioRelayService.STREAM_KEY, AudioManager.STREAM_MUSIC);
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
//        Integer streamType = Objects.requireNonNull(mViewModel.getStreamType().getValue());

        startService(audioRelayServiceIntent);
        mViewModel.setAppMode(AppStateViewModel.AppMode.AMPLIFYING_ON);
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
        mViewModel.setAppMode(AppStateViewModel.AppMode.AMPLIFYING_OFF);
    }

    private void updateAppMode() {
        if (!mBluetoothDeviceConnected || !mScoAudioConnected)
            mViewModel.setAppMode(AppStateViewModel.AppMode.NO_MIC_CONNECTED);
        else if (mViewModel.getAppMode().getValue() != AppStateViewModel.AppMode.AMPLIFYING_ON) {
            mViewModel.setAppMode(AppStateViewModel.AppMode.AMPLIFYING_OFF);
        }
    }

    private void activateBluetoothScoIfNecessary() {
        if (!mBluetoothDeviceConnected || mScoAudioConnected) {
            return;
        }
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