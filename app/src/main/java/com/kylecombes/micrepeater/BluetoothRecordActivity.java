package com.kylecombes.micrepeater;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sample that demonstrates how to record from a Bluetooth HFP microphone using {@link AudioRecord}.
 */
public class BluetoothRecordActivity extends Activity {

    private static final String TAG = BluetoothRecordActivity.class.getCanonicalName();

    private static final int SAMPLING_RATE_IN_HZ = 44100;

    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * Factor by that the minimum buffer size is multiplied. The bigger the factor is the less
     * likely it is that samples will be dropped, but more memory will be used. The minimum buffer
     * size is determined by {@link AudioRecord#getMinBufferSize(int, int, int)} and depends on the
     * recording settings.
     */
    private static final int BUFFER_SIZE_FACTOR = 2;

    /**
     * Size of the buffer where the audio data is stored by Android
     */
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR;

    /**
     * Signals whether a recording is in progress (true) or not (false).
     */
    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);

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

    private AudioRecord recorder = null;

    private AudioManager audioManager;

    private Thread recordingThread = null;

    private Button startButton;

    private Button stopButton;

    private Button bluetoothButton;

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

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

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

    @Override
    protected void onPause() {
        super.onPause();

        stopRecording();
        unregisterReceiver(bluetoothStateReceiver);
    }

    private void startRecording() {
        // Depending on the device one might has to change the AudioSource, e.g. to DEFAULT
        // or VOICE_COMMUNICATION
        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

        recorder.startRecording();

        recordingInProgress.set(true);

        recordingThread = new Thread(new RecordingRunnable(), "Recording Thread");
        recordingThread.start();

        bluetoothButton.setEnabled(calculateBluetoothButtonState());
        startButton.setEnabled(calculateStartRecordButtonState());
        stopButton.setEnabled(calculateStopRecordButtonState());
    }

    private void stopRecording() {
        if (null == recorder) {
            return;
        }

        recordingInProgress.set(false);

        recorder.stop();

        recorder.release();

        recorder = null;

        recordingThread = null;

        bluetoothButton.setEnabled(calculateBluetoothButtonState());
        startButton.setEnabled(calculateStartRecordButtonState());
        stopButton.setEnabled(calculateStopRecordButtonState());
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

    private class RecordingRunnable implements Runnable {

        @Override
        public void run() {
            AudioTrack audio = new AudioTrack(AudioManager.STREAM_MUSIC,
                    SAMPLING_RATE_IN_HZ,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AUDIO_FORMAT,
                    BUFFER_SIZE,
                    AudioTrack.MODE_STREAM);
            final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            audio.play();

            while (recordingInProgress.get()) {
                int result = recorder.read(buffer, BUFFER_SIZE);
                if (result < 0) {
                    throw new RuntimeException("Reading of audio buffer failed: " +
                            getBufferReadFailureReason(result));
                }
                // TODO: Convert to write(byte[], int, int) for support of earlier Android?
                audio.write(buffer, BUFFER_SIZE, AudioTrack.WRITE_NON_BLOCKING);
//                outStream.write(buffer.array(), 0, BUFFER_SIZE);
                buffer.clear();
            }
        }

        private String getBufferReadFailureReason(int errorCode) {
            switch (errorCode) {
                case AudioRecord.ERROR_INVALID_OPERATION:
                    return "ERROR_INVALID_OPERATION";
                case AudioRecord.ERROR_BAD_VALUE:
                    return "ERROR_BAD_VALUE";
                case AudioRecord.ERROR_DEAD_OBJECT:
                    return "ERROR_DEAD_OBJECT";
                case AudioRecord.ERROR:
                    return "ERROR";
                default:
                    return "Unknown (" + errorCode + ")";
            }
        }
    }

    enum BluetoothState {
        AVAILABLE, UNAVAILABLE
    }
}
