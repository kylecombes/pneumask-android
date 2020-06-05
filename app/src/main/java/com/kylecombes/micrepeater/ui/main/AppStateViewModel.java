package com.kylecombes.micrepeater.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AppStateViewModel extends ViewModel {

    private MutableLiveData<Integer> mMicBatteryPercentage = new MutableLiveData<>();

    public void setMicBatteryPercentage(int percentage) {
        mMicBatteryPercentage.setValue(percentage);
    }

    public MutableLiveData<Integer> getMicBatteryPercentage() {
        return mMicBatteryPercentage;
    }
}