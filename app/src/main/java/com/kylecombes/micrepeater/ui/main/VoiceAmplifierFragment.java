package com.kylecombes.micrepeater.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.kylecombes.micrepeater.R;
import com.kylecombes.micrepeater.VoiceAmplificationController;

import java.util.Objects;

public class VoiceAmplifierFragment extends Fragment {

    private AppStateViewModel pageViewModel;
    private TextView mBatteryStatusTextView;
    private Button mStartStopButton;
    private VoiceAmplificationController mAmpController;

    static VoiceAmplifierFragment newInstance() {
        return new VoiceAmplifierFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(AppStateViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof VoiceAmplificationController)) {
            throw new RuntimeException(context.getClass() + " must implement " + VoiceAmplificationController.class);
        }
        mAmpController = (VoiceAmplificationController)context;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voice_amplifier, container, false);
        mBatteryStatusTextView = root.findViewById(R.id.status_box_battery_level_tv);
        mStartStopButton = root.findViewById(R.id.voice_amplifier_start_stop_button);
        pageViewModel.getMicBatteryPercentage().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer p) {
                setBatteryLevelText(p);
            }
        });
        pageViewModel.getMicIsOn().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean b) {
                if (b) {
                    mStartStopButton.setText(R.string.stop);

                } else {
                    mStartStopButton.setText(R.string.start);
                }
            }
        });
        root.findViewById(R.id.voice_amplifier_start_stop_button).setOnClickListener(startStopButtonClickedListener);
        return root;
    }

    private void setBatteryLevelText(Integer percentage) {
        percentage = Math.max(0, Math.min(percentage, 100));
        String percentageText = getString(R.string.percentage, percentage);
        mBatteryStatusTextView.setText(percentageText);
    }

    private View.OnClickListener startStopButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Boolean amplificationActive = Objects.requireNonNull(pageViewModel.getMicIsOn().getValue());
            if (amplificationActive) {
                mAmpController.stopAmplification();
            } else {
                mAmpController.startAmplification();
            }
        }
    };

}