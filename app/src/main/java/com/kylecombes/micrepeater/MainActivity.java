package com.kylecombes.micrepeater;

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
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Sample that demonstrates how to record from a Bluetooth HFP microphone using {@link AudioRecord}.
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private AudioManager audioManager;
    Intent audioRelayServiceIntent;

    boolean mBluetoothAvailable = false;
    boolean recordingInProgress = false;

    // View elements
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
                        if (!bluetoothAvailable) {
                            stopRecording();
                        }
                        updateViewStates();
                    }
                }
        );

        // Find our view elements so we can change their properties later
        bluetoothStatusTV = findViewById(R.id.textView_main_bluetoothStatus);
        startButton = findViewById(R.id.button_main_start);
        stopButton = findViewById(R.id.button_main_stop);
    }

    public void onStartButtonPressed(View v) {
        startAudioService();

        updateViewStates();
    }

    public void onStopButtonPressed(View v) {
        stopRecording();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Make sure we're sending audio over Bluetooth
        activateBluetoothSco();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (AudioRelayService.service != null) {
            recordingInProgress = AudioRelayService.service.recordingInProgress();
        } else {
            recordingInProgress = false;
        }
        updateViewStates();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // If Bluetooth SCO is on but we're not recording, shut it off
        if (audioManager != null && audioManager.isBluetoothScoOn() && AudioRelayService.service == null) {
            audioManager.stopBluetoothSco();
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

    private void stopRecording() {
        if (AudioRelayService.service != null) {
            AudioRelayService.service.shutDown();
            recordingInProgress = false;
        }

        updateViewStates();
    }

    private void updateViewStates() {
        if (mBluetoothAvailable) {
            bluetoothStatusTV.setText(R.string.bluetooth_available);
        } else {
            bluetoothStatusTV.setText(R.string.bluetooth_unavailable);
        }
        startButton.setEnabled(mBluetoothAvailable && !recordingInProgress);
        stopButton.setEnabled(mBluetoothAvailable && recordingInProgress);
    }

    enum BluetoothState {
        AVAILABLE,
        INITIALIZING,
        UNAVAILABLE,
    }

}
