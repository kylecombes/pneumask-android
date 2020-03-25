package com.kylecombes.micrepeater;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Sample that demonstrates how to record from a Bluetooth HFP microphone using {@link AudioRecord}.
 */
public class BluetoothRecordActivity extends Activity {

    private static final String TAG = BluetoothRecordActivity.class.getCanonicalName();
    /*
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
     */

    Button startButton;

    Button stopButton;

    Intent audioRelayServiceIntent;

//    private Button bluetoothButton;

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
        /*
        bluetoothButton = (Button) findViewById(R.id.btnBluetooth);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateBluetoothSco();
            }
        });*/
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();

        bluetoothButton.setEnabled(calculateBluetoothButtonState());
        startButton.setEnabled(calculateStartRecordButtonState());
        stopButton.setEnabled(calculateStopRecordButtonState());

        registerReceiver(bluetoothStateReceiver, new IntentFilter(
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
    }
    */

    private void startRecording() {
        audioRelayServiceIntent = new Intent(this, AudioRelayService.class);
        // TODO: Should run this as a foreground service so there's a persistent notification with a
        // stop button https://developer.android.com/guide/components/services#Foreground
        startService(audioRelayServiceIntent);
    }

    private void stopRecording() {
        stopService(audioRelayServiceIntent);
    }
    /*
    private void activateBluetoothSco() {
        if (!audioManager.isBluetoothScoAvailableOffCall()) {
            Log.e(TAG, "SCO ist not available, recording is not possible");
            return;
        }

        if (!audioManager.isBluetoothScoOn()) {
            audioManager.startBluetoothSco();
        }
    }

    private void bluetoothStateChanged(BluetoothState state) {
        Log.i(TAG, "Bluetooth state changed to:" + state);

        if (BluetoothState.UNAVAILABLE == state && recordingInProgress.get()) {
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
        return audioManager.isBluetoothScoOn() && !recordingInProgress.get();
    }

    private boolean calculateStopRecordButtonState() {
        return audioManager.isBluetoothScoOn() && recordingInProgress.get();
    }

    enum BluetoothState {
        AVAILABLE, UNAVAILABLE
    }
     */
}
