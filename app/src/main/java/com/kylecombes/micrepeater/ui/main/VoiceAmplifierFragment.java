package com.kylecombes.micrepeater.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.kylecombes.micrepeater.R;

public class VoiceAmplifierFragment extends Fragment {

    private AppStateViewModel pageViewModel;
    private TextView mBatteryStatusTextView;

    public static VoiceAmplifierFragment newInstance() {
        return new VoiceAmplifierFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(AppStateViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voice_amplifier, container, false);
        pageViewModel.getMicBatteryPercentage().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer p) {
                setBatteryLevelText(p);
            }
        });
        mBatteryStatusTextView = root.findViewById(R.id.status_box_battery_level_tv);
        return root;
    }

    public void setBatteryLevelText(Integer percentage) {
        percentage = Math.max(0, Math.min(percentage, 100));
        String percentageText = getString(R.string.percentage, percentage);
        mBatteryStatusTextView.setText(percentageText);
    }
}