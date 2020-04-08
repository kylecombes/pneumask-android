package com.pint.micrepeater;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import com.google.firebase.analytics.FirebaseAnalytics;

import static java.lang.System.currentTimeMillis;

/**
 * Sample that demonstrates how to record from a Bluetooth HFP microphone using {@link AudioRecord}.
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getCanonicalName();
//    FirebaseAnalytics mFirebaseAnalytics;

    private AudioManager audioManager;
    Intent audioRelayServiceIntent;

    boolean mBluetoothAvailable = false;
    boolean recordingInProgress = false;
    long startTime;

    // View elements
    ImageView bluetoothIcon;
    TextView bluetoothStatusTV;
    Button startButton;
    Button stopButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        requestNeededPermissions();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Register a listener to respond to Bluetooth connect/disconnect events
        registerReceiver(BluetoothStateReceiver.getInstance(),
                new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));

        // Register a callback to get notified about Bluetooth connect/disconnect events
        BluetoothStateReceiver.getInstance().registerStateChangeReceiver(
                new BluetoothStateReceiver.StateChangeReceiver() {
                    public void stateChanged(boolean bluetoothAvailable) {
                        mBluetoothAvailable = bluetoothAvailable;
                        if (recordingInProgress && !bluetoothAvailable) {
                            stopRecording();
                        }
                        updateViewStates();
                    }
                }
        );

        // Find our view elements so we can change their properties later
        bluetoothIcon = findViewById(R.id.imageView_main_bluetooth);
        bluetoothStatusTV = findViewById(R.id.textView_main_bluetoothStatus);
        startButton = findViewById(R.id.button_main_start);
        stopButton = findViewById(R.id.button_main_stop);

        // Log events and crashes
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public void onStartButtonPressed(View v) {
        startAudioService();
        updateViewStates();

        // Log this start button press in Firebase
        Bundle bundle = new Bundle();
        bundle.putString("RelayingControlAction", "start");
//        mFirebaseAnalytics.logEvent("RelayingButtonPress", bundle);
        startTime = currentTimeMillis();
    }

    public void onStopButtonPressed(View v) {
        stopRecording();

        // Log this stop button press in Firebase
        long elapsedTimeS = (currentTimeMillis() - startTime) / 1000;
        Bundle bundle = new Bundle();
        bundle.putString("RelayingControlAction", "stop");
        bundle.putInt("ElapsedSeconds", (int)elapsedTimeS);
//        mFirebaseAnalytics.logEvent("RelayingButtonPress", bundle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        activateBluetoothSco();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AudioRelayService ars = AudioRelayService.getInstance();
        if (ars != null) {
            recordingInProgress = ars.recordingInProgress();
        } else {
            recordingInProgress = false;
        }
        updateViewStates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!recordingInProgress && !isChangingConfigurations()) {
            audioManager.stopBluetoothSco();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!recordingInProgress) {
            unregisterReceiver(BluetoothStateReceiver.getInstance());
        }
    }

    /**
     * Checks to make sure the app has the needed permissions:
     *   - RECORD_AUDIO
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

    /**
     * Attempts to start Bluetooth SCO (used for streaming audio to and from Bluetooth devices).
     */
    private void activateBluetoothSco() {
        if (audioManager == null || !audioManager.isBluetoothScoAvailableOffCall()) {
            Log.e(TAG, "SCO ist not available. Recording is not possible");
            mBluetoothAvailable = false;
            return;
        }

        if (!audioManager.isBluetoothScoOn()) {
            audioManager.startBluetoothSco();
        }
    }

    /**
     * Starts the AudioRelayService.
     */
    private void startAudioService() {
        audioRelayServiceIntent = new Intent(this, AudioRelayService.class);
        audioRelayServiceIntent.putExtra(AudioRelayService.STREAM_KEY,
                audioManager.isWiredHeadsetOn() ? AudioManager.STREAM_MUSIC : AudioManager.STREAM_ALARM);
        startService(audioRelayServiceIntent);
        recordingInProgress = true;
    }

    /**
     * Stops the AudioRelayService.
     */
    private void stopRecording() {
        AudioRelayService ars = AudioRelayService.getInstance();
        if (ars != null) {
            ars.shutDown();
        }
        recordingInProgress = false;

        updateViewStates();
    }

    /**
     * Refreshes the appearance (e.g. text, enabled/disabled) of Buttons, TextViews, etc according
     * to the current state of the application.
     */
    private void updateViewStates() {
        if (mBluetoothAvailable) {
            // Display Bluetooth icon at full opacity
            bluetoothIcon.setImageAlpha(255);
            bluetoothStatusTV.setText(R.string.bluetooth_available);
        } else {
            // Make Bluetooth icon 50% transparent
            bluetoothIcon.setImageAlpha(128);
            bluetoothStatusTV.setText(R.string.bluetooth_unavailable);
        }
        startButton.setEnabled(mBluetoothAvailable && !recordingInProgress);
        stopButton.setEnabled(mBluetoothAvailable && recordingInProgress);
    }

}
