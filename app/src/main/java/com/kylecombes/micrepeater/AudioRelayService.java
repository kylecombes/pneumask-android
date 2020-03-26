package com.kylecombes.micrepeater;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioRelayService extends Service {

    private AudioManager audioManager;
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

    private AudioRecord recorder = null;

    private Thread recordingThread = null;

    public AudioRelayService(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "AudioRelayService starting", Toast.LENGTH_SHORT).show();

        return Service.START_STICKY;
    }

    /* Binding service to main activity */

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        AudioRelayService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AudioRelayService.this;
        }
    }
//
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startRecording() {
        // Depending on the device one might has to change the AudioSource, e.g. to DEFAULT
        // or VOICE_COMMUNICATION
        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

        recorder.startRecording();

        recordingInProgress.set(true);

        recordingThread = new Thread(new RecordingRunnable(), "Recording Thread");
        recordingThread.start();

    }

    public void stopRecording() {
        if (null == recorder) {
            return;
        }

        recordingInProgress.set(false);

        recorder.stop();

        recorder.release();

        recorder = null;

        recordingThread = null;

        Toast.makeText(this, "Stopped Recording", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        stopRecording();

        Toast.makeText(this, "Destroyed AudioRelayService", Toast.LENGTH_SHORT).show();
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
                audio.write(buffer.array(), 0, BUFFER_SIZE);
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

    public AudioManager getAudioManager(){
        return audioManager;
    }

    public void setAudioManager(AudioManager AM) {
        this.audioManager = AM;
    }

    public boolean recordingInProgress() {
        return recordingInProgress.get();
    }

}
