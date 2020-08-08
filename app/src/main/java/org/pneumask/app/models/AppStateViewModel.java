package org.pneumask.app.models;

import android.media.AudioManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AppStateViewModel extends ViewModel {

    public enum AppMode {
        NO_MIC_CONNECTED,
        AMPLIFYING_OFF,
        AMPLIFYING_ON,
    }

    private MutableLiveData<AppMode> mAppMode = new MutableLiveData<>(AppMode.NO_MIC_CONNECTED);
    private MutableLiveData<Integer> mMicBatteryPercentage = new MutableLiveData<>();
    private MutableLiveData<Integer> mStreamType = new MutableLiveData<>(AudioManager.STREAM_VOICE_CALL);

    public void setAppMode(AppMode appMode) {
        mAppMode.setValue(appMode);
    }

    public void setMicBatteryPercentage(Integer percentage) {
        mMicBatteryPercentage.setValue(percentage);
    }

    public LiveData<AppMode> getAppMode() {
        return mAppMode;
    }

    public LiveData<Integer> getMicBatteryPercentage() {
        return mMicBatteryPercentage;
    }

    public LiveData<Integer> getStreamType() {
        return mStreamType;
    }
}