package com.kylecombes.micrepeater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.util.Log;

public class BluetoothStateReceiver extends BroadcastReceiver {

    private static final String TAG = BluetoothStateReceiver.class.getCanonicalName();

    private static BluetoothStateReceiver mInstance = null;
    private static AudioManager mAudioManager;

    private StateChangeReceiver stateChangeReceiver = null;

    public static BluetoothStateReceiver getInstance(AudioManager audioManager) {
        if (audioManager != null) {
            mAudioManager = audioManager;
        }
        if (mInstance == null) {
            mInstance = new BluetoothStateReceiver();
        }
        return mInstance;
    }

    public void registerStateChangeReceiver(StateChangeReceiver receiver) {
        stateChangeReceiver = receiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null | stateChangeReceiver == null)
            return;
        if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
            boolean scoAudioConnected = false;
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            switch (state) {
                case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                    Log.i(TAG, "Bluetooth HFP Headset is connected");
                    scoAudioConnected = true;
                    break;
                case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                    Log.i(TAG, "Bluetooth HFP Headset is connecting");
                    scoAudioConnected = false;
                    break;
                case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                    Log.i(TAG, "Bluetooth HFP Headset is disconnected");
                    scoAudioConnected = false;
                    break;
                case AudioManager.SCO_AUDIO_STATE_ERROR:
                    Log.i(TAG, "Bluetooth HFP Headset is in error state");
                    scoAudioConnected = false;
                    break;
                default:
                    // Don't trigger any notifications
                    break;
            }
            stateChangeReceiver.scoAudioConnectionStateChange(scoAudioConnected);
        } else if (action.equals("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")) {
            // Handle Bluetooth device battery level messages
            int batteryLevel = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1);
            if (batteryLevel != -1) {
                stateChangeReceiver.batteryLevelChanged(batteryLevel);
            }
        }
        // Check if a Bluetooth SCO mic is connected (Android M and above only)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean deviceConnected = false;
            AudioDeviceInfo[] microphones = mAudioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
            for (AudioDeviceInfo device : microphones) {
                if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                    deviceConnected = true;
                    break;
                }
            }
            stateChangeReceiver.bluetoothDeviceConnectionStateChange(deviceConnected);
        }
    }

    public interface StateChangeReceiver {
        void bluetoothDeviceConnectionStateChange(boolean isConnected);
        void scoAudioConnectionStateChange(boolean isConnected);
        void batteryLevelChanged(Integer batteryLevel);
    }

}
