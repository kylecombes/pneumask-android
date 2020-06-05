package com.kylecombes.micrepeater.ui.main;

import android.media.AudioManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AppStateViewModel extends ViewModel {

    private MutableLiveData<Integer> mMicBatteryPercentage = new MutableLiveData<>();
    private MutableLiveData<Boolean> mMicIsOn = new MutableLiveData<>(false);
    private MutableLiveData<Integer> mStreamType = new MutableLiveData<>(AudioManager.STREAM_VOICE_CALL);

    public void setMicBatteryPercentage(int percentage) {
        mMicBatteryPercentage.setValue(percentage);
    }

    public void setMicIsOn(boolean isOn) {
        mMicIsOn.setValue(isOn);
    }

    public LiveData<Integer> getMicBatteryPercentage() {
        return mMicBatteryPercentage;
    }

    public LiveData<Boolean> getMicIsOn() {
        return mMicIsOn;
    }

    public LiveData<Integer> getStreamType() {
        return mStreamType;
    }
}