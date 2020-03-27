package com.kylecombes.micrepeater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class BluetoothStateReceiver extends BroadcastReceiver {

    private static final String TAG = BluetoothStateReceiver.class.getCanonicalName();

    private static BluetoothStateReceiver mInstance = null;
    private boolean mBluetoothAvailable = false;

    private StateChangeReceiver stateChangeReceiver = null;

    public static BluetoothStateReceiver getInstance() {
        if (mInstance == null) {
            mInstance = new BluetoothStateReceiver();
        }
        return mInstance;
    }

    public boolean getBluetoothAvailable() {
        return mBluetoothAvailable;
    }

    public void registerStateChangeReceiver(StateChangeReceiver receiver) {
        stateChangeReceiver = receiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
        switch (state) {
            case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                Log.i(TAG, "Bluetooth HFP Headset is connected");
                mBluetoothAvailable = true;
                break;
            case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                Log.i(TAG, "Bluetooth HFP Headset is connecting");
                mBluetoothAvailable = false;
            case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                Log.i(TAG, "Bluetooth HFP Headset is disconnected");
                mBluetoothAvailable = false;
                break;
            case AudioManager.SCO_AUDIO_STATE_ERROR:
                Log.i(TAG, "Bluetooth HFP Headset is in error state");
                mBluetoothAvailable = false;
                break;
            default:
                // Don't trigger any notifications
                return;
        }
        if (stateChangeReceiver != null) {
            stateChangeReceiver.stateChanged(mBluetoothAvailable);
        }
    }

    public interface StateChangeReceiver {
        void stateChanged(boolean bluetoothAvailable);
    }

}
