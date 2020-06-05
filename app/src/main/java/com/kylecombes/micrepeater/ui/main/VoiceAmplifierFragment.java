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
import androidx.lifecycle.ViewModelProvider;

import com.kylecombes.micrepeater.R;

import java.util.Objects;

public class VoiceAmplifierFragment extends Fragment {

    private AppStateViewModel pageViewModel;
    private TextView mBatteryStatusTextView;

    static VoiceAmplifierFragment newInstance() {
        return new VoiceAmplifierFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(AppStateViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voice_amplifier, container, false);
        pageViewModel.getMicBatteryPercentage().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer p) {
                setBatteryLevelText(p);
            }
        });
        mBatteryStatusTextView = root.findViewById(R.id.status_box_battery_level_tv);
        return root;
    }

    private void setBatteryLevelText(Integer percentage) {
        percentage = Math.max(0, Math.min(percentage, 100));
        String percentageText = getString(R.string.percentage, percentage);
        mBatteryStatusTextView.setText(percentageText);
    }
}