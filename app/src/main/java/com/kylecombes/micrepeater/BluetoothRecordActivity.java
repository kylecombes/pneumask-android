package com.kylecombes.micrepeater;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Sample that demonstrates how to record from a Bluetooth HFP microphone using {@link AudioRecord}.
 */
public class BluetoothRecordActivity extends Activity {

    private AudioManager audioManager;
    Intent audioRelayServiceIntent;

    // Buttons
    Button startButton;
    Button stopButton;
    private Button bluetoothButton;

    private static final String TAG = BluetoothRecordActivity.class.getCanonicalName();

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {

        private BluetoothState bluetoothState = BluetoothState.UNAVAILABLE;

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            switch (state) {
                case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                    Log.i(TAG, "Bluetooth HFP Headset is connected");
                    handleBluetoothStateChange(BluetoothState.AVAILABLE);
                    break;
                case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                    Log.i(TAG, "Bluetooth HFP Headset is connecting");
                    handleBluetoothStateChange(BluetoothState.UNAVAILABLE);
                case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                    Log.i(TAG, "Bluetooth HFP Headset is disconnected");
                    handleBluetoothStateChange(BluetoothState.UNAVAILABLE);
                    break;
                case AudioManager.SCO_AUDIO_STATE_ERROR:
                    Log.i(TAG, "Bluetooth HFP Headset is in error state");
                    handleBluetoothStateChange(BluetoothState.UNAVAILABLE);
                    break;
            }
        }

        private void handleBluetoothStateChange(BluetoothState state) {
            if (bluetoothState == state) {
                return;
            }

            bluetoothState = state;
            bluetoothStateChanged(state);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1234);
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        startButton = (Button) findViewById(R.id.btnStart);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        stopButton = (Button) findViewById(R.id.btnStop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        bluetoothButton = (Button) findViewById(R.id.btnBluetooth);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateBluetoothSco();
                updateButtonStates();
            }
        });
    }

    private void updateButtonStates() {
        bluetoothButton.setEnabled(calculateBluetoothButtonState());
        startButton.setEnabled(calculateStartRecordButtonState());
        stopButton.setEnabled(calculateStopRecordButtonState());
    }


    @Override
    protected void onResume() {
        super.onResume();

        updateButtonStates();

        registerReceiver(bluetoothStateReceiver, new IntentFilter(
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(bluetoothStateReceiver);
    }

    private void startAudioService() {
        audioRelayServiceIntent = new Intent(this, AudioRelayService.class);
        startService(audioRelayServiceIntent);
    }

    private void startRecording() {
        startAudioService();

        // Update the button states
        bluetoothButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopRecording() {
        if (AudioRelayService.service != null) {
            AudioRelayService.service.shutDown();
        }

        // Update the button states
        bluetoothButton.setEnabled(calculateBluetoothButtonState());
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void activateBluetoothSco() {
        if (audioManager == null || !audioManager.isBluetoothScoAvailableOffCall()) {
            Log.e(TAG, "SCO ist not available. Recording is not possible");
            return;
        }

        if (!audioManager.isBluetoothScoOn()) {
            audioManager.startBluetoothSco();
        }
    }

    private void bluetoothStateChanged(BluetoothState state) {
        Log.i(TAG, "Bluetooth state changed to: " + state);

        AudioRelayService ars = AudioRelayService.service;

        if (BluetoothState.UNAVAILABLE == state && ars != null && ars.recordingInProgress()) {
            stopRecording();
        }

        bluetoothButton.setEnabled(calculateBluetoothButtonState());
        startButton.setEnabled(calculateStartRecordButtonState());
        stopButton.setEnabled(calculateStopRecordButtonState());
    }

    private boolean calculateBluetoothButtonState() {
        return !audioManager.isBluetoothScoOn();
    }

    private boolean calculateStartRecordButtonState() {
        return audioManager.isBluetoothScoOn() && AudioRelayService.service == null;
    }

    private boolean calculateStopRecordButtonState() {
        return audioManager.isBluetoothScoOn() && AudioRelayService.service != null;
    }

    enum BluetoothState {
        AVAILABLE, UNAVAILABLE
    }

}
