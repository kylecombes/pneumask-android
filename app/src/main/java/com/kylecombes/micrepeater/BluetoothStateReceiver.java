package com.kylecombes.micrepeater;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class BluetoothStateReceiver extends BroadcastReceiver {

    private static final String TAG = BluetoothStateReceiver.class.getCanonicalName();

    private static BluetoothStateReceiver mInstance = null;

    private StateChangeReceiver stateChangeReceiver = null;

    public static BluetoothStateReceiver getInstance() {
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
        boolean mScoAudioConnected = false;
        boolean mDeviceConnected;
        Integer batteryLevel = null;
        String action = intent.getAction();
        if (action == null)
            return;
        if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            switch (state) {
                case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                    Log.i(TAG, "Bluetooth HFP Headset is connected");
                    mScoAudioConnected = true;
                    break;
                case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                    Log.i(TAG, "Bluetooth HFP Headset is connecting");
                    mScoAudioConnected = false;
                    break;
                case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                    Log.i(TAG, "Bluetooth HFP Headset is disconnected");
                    mScoAudioConnected = false;
                    break;
                case AudioManager.SCO_AUDIO_STATE_ERROR:
                    Log.i(TAG, "Bluetooth HFP Headset is in error state");
                    mScoAudioConnected = false;
                    break;
                default:
                    // Don't trigger any notifications
                    return;
            }
        } else if (action.equals("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")) {
            batteryLevel = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1);
            if (batteryLevel == -1) {
                batteryLevel = null;
            }
            Log.i(TAG, "Battery level: " + batteryLevel);
        }

        mDeviceConnected = mScoAudioConnected || BluetoothDevice.ACTION_ACL_CONNECTED.equals(action);
        if (stateChangeReceiver != null) {
            stateChangeReceiver.stateChanged(mDeviceConnected, mScoAudioConnected, batteryLevel);
        }
    }

    public interface StateChangeReceiver {
        void stateChanged(boolean deviceConnected, boolean scoAudioConnected, Integer batteryLevel);
    }

}
