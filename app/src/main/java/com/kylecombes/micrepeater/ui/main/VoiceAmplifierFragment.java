package com.kylecombes.micrepeater.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.kylecombes.micrepeater.R;
import com.kylecombes.micrepeater.VoiceAmplificationController;

import java.util.Objects;

public class VoiceAmplifierFragment extends Fragment {

    private AppStateViewModel pageViewModel;
    private TextView mDeviceStatusTitleTextView;
    private TextView mDeviceStatusTextView;
    private TextView mBatteryStatusTitleTextView;
    private TextView mBatteryStatusTextView;
    private Button mStartStopButton;
    private ImageView mStatusImageView;
    private AmplifyingControlTile mAmplifyingControlTile;
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
        mDeviceStatusTitleTextView = root.findViewById(R.id.status_box_status_title_tv);
        mDeviceStatusTextView = root.findViewById(R.id.status_box_status_tv);
        mBatteryStatusTitleTextView = root.findViewById(R.id.status_box_battery_level_title_tv);
        mBatteryStatusTextView = root.findViewById(R.id.status_box_battery_level_tv);
        mAmplifyingControlTile = root.findViewById(R.id.amplifying_control_tile);
        mStartStopButton = root.findViewById(R.id.amplifying_control_start_stop_button);
        mStartStopButton.setOnClickListener(startStopButtonClickedListener);
        mStatusImageView = root.findViewById(R.id.voice_amplifier_status_iv);

        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        pageViewModel.getBluetoothAudioConnected().observe(lifecycleOwner, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean deviceConnected) {
                updateConnectedText(deviceConnected);
            }
        });
        pageViewModel.getMicBatteryPercentage().observe(lifecycleOwner, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer p) {
                updateBatteryLevelText(p);
            }
        });
        pageViewModel.getMicIsOn().observe(lifecycleOwner, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean b) {
                mAmplifyingControlTile.setAmplifyingActive(b);
                if (b) {
                    mStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.amplifying_on));

                } else {
                    mStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.amplifying_off));
                }
            }
        });
        return root;
    }

    private void updateConnectedText(Boolean isDeviceConnected) {
        if (isDeviceConnected) {
            mDeviceStatusTextView.setText(R.string.connected);
            mDeviceStatusTextView.setTextColor(getResources().getColor(R.color.green));
            mDeviceStatusTitleTextView.setTextColor(getResources().getColor(R.color.green));
        } else {
            mDeviceStatusTextView.setText(R.string.disconnected);
            mDeviceStatusTextView.setTextColor(getResources().getColor(R.color.red));
            mDeviceStatusTitleTextView.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private void updateBatteryLevelText(Integer percentage) {
        if (percentage == null) {
            mBatteryStatusTextView.setVisibility(View.GONE);
            mBatteryStatusTitleTextView.setVisibility(View.GONE);
        } else {
            mBatteryStatusTextView.setVisibility(View.VISIBLE);
            mBatteryStatusTitleTextView.setVisibility(View.VISIBLE);
            percentage = Math.max(0, Math.min(percentage, 100));
            String percentageText = getString(R.string.percentage, percentage);
            mBatteryStatusTextView.setText(percentageText);
        }
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