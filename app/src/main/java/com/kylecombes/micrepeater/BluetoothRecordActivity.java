package com.kylecombes.micrepeater;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sample that demonstrates how to record from a Bluetooth HFP microphone using {@link AudioRecord}.
 */
public class BluetoothRecordActivity extends Activity {

    private AudioManager audioManager;

    boolean mBound = false;
    AudioRelayService audioRelayService;

    Intent audioRelayServiceIntent;

    // Buttons
    Button startButton;
    Button stopButton;
    private Button bluetoothButton;

    /**
     * Signals whether a recording is in progress (true) or not (false).
     */
    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);

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

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioRelayService.LocalBinder binder = (AudioRelayService.LocalBinder) service;
            audioRelayService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        // This happens after onCreate()
        super.onStart();
        // Bind to LocalService
        audioRelayServiceIntent = new Intent(this, AudioRelayService.class);
        bindService(audioRelayServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }


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
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        bluetoothButton.setEnabled(calculateBluetoothButtonState());
        startButton.setEnabled(calculateStartRecordButtonState());
        stopButton.setEnabled(calculateStopRecordButtonState());

        registerReceiver(bluetoothStateReceiver, new IntentFilter(
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
    }

    private void startRecording() {
        // Boolean signal that recording has started
        recordingInProgress.set(true);
        if(mBound){
            audioRelayService.setAudioManager(audioManager);
            Toast.makeText(getApplicationContext(), "AM Sent", Toast.LENGTH_LONG).show();
        }

        // TODO: Should run this as a foreground service so there's a persistent notification with a
        // stop button https://developer.android.com/guide/components/services#Foreground
        audioRelayServiceIntent = new Intent(this, AudioRelayService.class);
        startService(audioRelayServiceIntent);

        // Update the button states
        bluetoothButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopRecording() {
        // Boolean signal that recording has ended
        recordingInProgress.set(false);

        audioRelayService.stopRecording();

        // Update the button states
//        bluetoothButton.setEnabled(calculateBluetoothButtonState());
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

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

}
