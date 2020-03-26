package com.kylecombes.micrepeater;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.MicrophoneDirection;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioRelayService extends Service {

    private AudioManager audioManager;
    private static final int SAMPLING_RATE_IN_HZ = getMinSupportedSampleRate();


    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * Size of the buffer where the audio data is stored by Android
     */
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG, AUDIO_FORMAT);

    /**
     * Signals whether a recording is in progress (true) or not (false).
     */
    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);

    private AudioRecord recorder = null;

    private Thread recordingThread = null;

    public AudioRelayService(){
        Log.d("AudioRelayService", "Sampling rate: " + SAMPLING_RATE_IN_HZ + " Hz");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "AudioRelayService starting", Toast.LENGTH_SHORT).show();

        startRecording();

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

        improveRecorder(recorder);
        recorder.startRecording();

        recordingInProgress.set(true);

        recordingThread = new Thread(new RecordingRunnable(), "Recording Thread");
        recordingThread.start();

    }

    private void improveRecorder(AudioRecord recorder) {
        int audioSessionId = recorder.getAudioSessionId();

        if(NoiseSuppressor.isAvailable())
        {
              NoiseSuppressor.create(audioSessionId);
        }
//        if(AutomaticGainControl.isAvailable())
//        {
//             AutomaticGainControl.create(audioSessionId);
//        }
        if(AcousticEchoCanceler.isAvailable()){
             AcousticEchoCanceler.create(audioSessionId);
        }
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

    private static int getMinSupportedSampleRate() {
        /*
         * Valid Audio Sample rates
         *
         * @see <a
         * href="http://en.wikipedia.org/wiki/Sampling_%28signal_processing%29"
         * >Wikipedia</a>
         */
        final int[] validSampleRates = new int[] { 8000, 11025, 16000, 22050,
                32000, 37800, 44056, 44100, 47250, 48000, 50000, 50400, 88200,
                96000, 176400, 192000, 352800, 2822400, 5644800 };
        /*
         * Selecting default audio input source for recording since
         * AudioFormat.CHANNEL_CONFIGURATION_DEFAULT is deprecated and selecting
         * default encoding format.
         */
        for (int validSampleRate : validSampleRates) {
            int result = AudioRecord.getMinBufferSize(validSampleRate,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT);
            if (result > 0) {
                // return the minimum supported audio sample rate
                return validSampleRate;
            }
        }
        // If none of the sample rates are supported return -1 handle it in
        // calling method
        return -1;
    }
}
