package com.kylecombes.micrepeater.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.kylecombes.micrepeater.R;
import com.kylecombes.micrepeater.interfaces.VoiceAmplificationController;
import com.kylecombes.micrepeater.widgets.AmplifyingControlTile;
import com.kylecombes.micrepeater.models.AppStateViewModel;

import java.util.Objects;

public class VoiceAmplifierFragment extends Fragment {

    private AppStateViewModel mPageViewModel;
    private TextView mDeviceStatusTitleLabelView;
    private TextView mDeviceStatusTextView;
    private TextView mBatteryLevelLabelTextView;
    private TextView mBatteryLevelTextView;
    private TextView mNoDeviceFoundTextView;
    private ImageView mStatusImageView;
    private AmplifyingControlTile mAmplifyingControlTile;
    private VoiceAmplificationController mAmpController;
    private static final String[] OUTPUTS = {"voice", "alarm", "music"};

    public static VoiceAmplifierFragment newInstance() {
        return new VoiceAmplifierFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(AppStateViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof VoiceAmplificationController)) {
            throw new RuntimeException(context.getClass() + " must implement " + VoiceAmplificationController.class);
        }
        mAmpController = (VoiceAmplificationController) context;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voice_amplifier, container, false);
        mDeviceStatusTitleLabelView = root.findViewById(R.id.status_box_status_label_tv);
        mDeviceStatusTextView = root.findViewById(R.id.status_box_status_tv);
        mBatteryLevelLabelTextView = root.findViewById(R.id.status_box_battery_level_label_tv);
        mBatteryLevelTextView = root.findViewById(R.id.status_box_battery_level_tv);
        mAmplifyingControlTile = root.findViewById(R.id.amplifying_control_tile);
        mNoDeviceFoundTextView = root.findViewById(R.id.voice_amplifier_no_device_detected_msg_tv);
        root.findViewById(R.id.amplifying_control_start_stop_button)
                .setOnClickListener(startStopButtonClickedListener);
        mStatusImageView = root.findViewById(R.id.voice_amplifier_status_iv);

        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        Spinner mDropdown = root.findViewById(R.id.audio_output_dropdown);
        // Create an adapter to describe how the items are displayed, adapters are used in several places in Android.
        // There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.audio_output_options, R.layout.spinner_item);
        //set the spinners adapter to the previously created one.
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mDropdown.setAdapter(adapter);
        mDropdown.setSelection(findDropdownPosition());
        mDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String audioOutput = OUTPUTS[position];
                getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
                        .edit().putString("audioOutput", audioOutput).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mPageViewModel.getAppMode().observe(lifecycleOwner, new Observer<AppStateViewModel.AppMode>() {
            @Override
            public void onChanged(AppStateViewModel.AppMode mode) {
                if (mode == AppStateViewModel.AppMode.NO_MIC_CONNECTED) {
                    mAmplifyingControlTile.setVisibility(View.GONE);
                    mNoDeviceFoundTextView.setVisibility(View.VISIBLE);
                    renderMicStatusWidget(false);
                } else {
                    mAmplifyingControlTile.setVisibility(View.VISIBLE);
                    mNoDeviceFoundTextView.setVisibility(View.GONE);
                    renderMicStatusWidget(true);
                    if (mode == AppStateViewModel.AppMode.AMPLIFYING_ON) {
                        mAmplifyingControlTile.setAmplifyingActive(true);
                    } else {
                        mAmplifyingControlTile.setAmplifyingActive(false);
                    }
                }
                updateBackgroundImage(mode);
            }
        });
        mPageViewModel.getMicBatteryPercentage().observe(lifecycleOwner, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer batteryPercentage) {
                updateBatteryLevelText(batteryPercentage);
            }
        });
        return root;
    }

    private void updateBackgroundImage(AppStateViewModel.AppMode appMode) {
        int imageDrawableId;
        switch (appMode) {
            case NO_MIC_CONNECTED:
                imageDrawableId = R.drawable.no_mic;
                break;
            case AMPLIFYING_OFF:
                imageDrawableId = R.drawable.amplifying_off;
                break;
            case AMPLIFYING_ON:
            default:
                imageDrawableId = R.drawable.amplifying_on;
                break;
        }
        mStatusImageView.setImageDrawable(getResources().getDrawable(imageDrawableId));
    }

    private void renderMicStatusWidget(boolean micConnected) {
        if (micConnected) {
            mDeviceStatusTextView.setText(R.string.connected);
            mDeviceStatusTextView.setTextColor(getResources().getColor(R.color.green));
            mDeviceStatusTitleLabelView.setTextColor(getResources().getColor(R.color.green));
            // If we have a battery percentage, display it
            if (mPageViewModel.getMicBatteryPercentage().getValue() != null) {
                setMicBatteryLevelTextVisible(true);
            }
        } else {
            mDeviceStatusTextView.setText(R.string.disconnected);
            mDeviceStatusTextView.setTextColor(getResources().getColor(R.color.red));
            mDeviceStatusTitleLabelView.setTextColor(getResources().getColor(R.color.red));
            setMicBatteryLevelTextVisible(false);
        }
    }

    private void setMicBatteryLevelTextVisible(boolean isVisible) {
        mBatteryLevelTextView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mBatteryLevelLabelTextView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void updateBatteryLevelText(Integer percentage) {
        setMicBatteryLevelTextVisible(percentage != null);
        if (percentage != null) {
            percentage = Math.max(0, Math.min(percentage, 100));
            // Set the text
            String percentageText = getString(R.string.percentage, percentage);
            mBatteryLevelTextView.setText(percentageText);
            // Set the color
            int color;
            if (percentage > 50) {
                color = getResources().getColor(R.color.green);
            } else if (percentage > 25) {
                color = getResources().getColor(R.color.yellow);
            } else {
                color = getResources().getColor(R.color.red);
            }
            mBatteryLevelLabelTextView.setTextColor(color);
            mBatteryLevelTextView.setTextColor(color);
        }
    }

    private int findDropdownPosition() {
        String outputPreference = getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
                .getString("audioOutput", "voice");
        for (int i = 0; i < OUTPUTS.length; ++i) {
            if (OUTPUTS[i].equals(outputPreference))
                return i;
        }
        return -1;
    }

    private View.OnClickListener startStopButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppStateViewModel.AppMode mode = Objects.requireNonNull(mPageViewModel.getAppMode().getValue());
            if (mode == AppStateViewModel.AppMode.AMPLIFYING_ON) {
                mAmpController.stopAmplification();
            } else {
                mAmpController.startAmplification();
            }
        }
    };

}